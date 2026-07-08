package com.socialmediablog.platform.services.interaction.application.usecase;

import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.ClapArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.interaction.application.command.RemoveBookmarkCommand;
import com.socialmediablog.platform.services.interaction.application.port.in.BookmarkArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.ClapArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.RemoveBookmarkUseCase;
import com.socialmediablog.platform.services.interaction.application.result.BookmarkView;
import com.socialmediablog.platform.services.interaction.application.result.ServiceStatus;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Interaction;
import com.socialmediablog.platform.services.interaction.domain.model.InteractionTargetType;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import com.socialmediablog.platform.services.interaction.domain.model.BookmarkStatus;
import com.socialmediablog.platform.services.interaction.domain.repository.BookmarkRepository;
import com.socialmediablog.platform.services.interaction.domain.repository.InteractionRepository;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import com.socialmediablog.platform.services.interaction.domain.vo.TargetId;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionApplicationService implements GetServiceStatusUseCase, BookmarkArticleUseCase, RemoveBookmarkUseCase, ClapArticleUseCase {

    private final BookmarkRepository bookmarkRepository;
    private final InteractionRepository interactionRepository;

    public InteractionApplicationService(
            BookmarkRepository bookmarkRepository,
            InteractionRepository interactionRepository
    ) {
        this.bookmarkRepository = bookmarkRepository;
        this.interactionRepository = interactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceStatus execute(GetServiceStatusCommand command) {
        return new ServiceStatus("interaction-service", "interactions", command.currentUserId());
    }

    @Override
    @Transactional
    public void execute(BookmarkArticleCommand command) {
        Instant now = Instant.now();
        InteractorId userId = InteractorId.of(command.userId());
        ArticleId articleId = ArticleId.of(command.articleId());

        bookmarkRepository.findByUserIdAndArticleId(userId, articleId)
                .map(existing -> existing.bookmark(now))
                .ifPresentOrElse(
                        updated -> {
                            if (updated != null) {
                                bookmarkRepository.save(updated);
                            }
                        },
                        () -> bookmarkRepository.save(Bookmark.create(userId, articleId, now))
                );
    }

    @Override
    @Transactional
    public void execute(RemoveBookmarkCommand command) {
        Instant now = Instant.now();
        InteractorId userId = InteractorId.of(command.userId());
        ArticleId articleId = ArticleId.of(command.articleId());

        bookmarkRepository.findByUserIdAndArticleId(userId, articleId)
                .map(existing -> existing.remove(now))
                .ifPresent(updated -> {
                    if (updated != null) {
                        bookmarkRepository.save(updated);
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkView> listBookmarks(UUID userId) {
        InteractorId interactorId = InteractorId.of(userId);
        return bookmarkRepository.findByUserId(interactorId).stream()
                .filter(bookmark -> bookmark.status() == BookmarkStatus.ACTIVE)
                .sorted(Comparator.comparing(Bookmark::bookmarkedAt).reversed())
                .map(BookmarkView::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarked(UUID userId, UUID articleId) {
        InteractorId interactorId = InteractorId.of(userId);
        ArticleId targetArticleId = ArticleId.of(articleId);
        return bookmarkRepository.findByUserIdAndArticleId(interactorId, targetArticleId)
                .map(bookmark -> bookmark.status() == BookmarkStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional
    public long execute(ClapArticleCommand command) {
        Instant now = Instant.now();
        InteractorId userId = InteractorId.of(command.userId());
        TargetId articleId = TargetId.of(command.articleId());

        interactionRepository.findByUserIdAndTarget(userId, InteractionTargetType.ARTICLE, articleId)
                .orElseGet(() -> interactionRepository.save(
                        Interaction.record(userId, InteractionTargetType.ARTICLE, articleId, 1, now)
                ));

        return interactionRepository.countByTarget(InteractionTargetType.ARTICLE, articleId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getArticleClapCount(java.util.UUID articleId) {
        TargetId targetId = TargetId.of(articleId);
        return interactionRepository.countByTarget(InteractionTargetType.ARTICLE, targetId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserClappedArticle(java.util.UUID userId, java.util.UUID articleId) {
        InteractorId interactorId = InteractorId.of(userId);
        TargetId targetId = TargetId.of(articleId);
        return interactionRepository.existsByUserIdAndTarget(interactorId, InteractionTargetType.ARTICLE, targetId);
    }
}
