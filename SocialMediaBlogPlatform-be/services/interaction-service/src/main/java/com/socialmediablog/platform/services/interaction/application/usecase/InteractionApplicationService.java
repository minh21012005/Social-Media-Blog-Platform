package com.socialmediablog.platform.services.interaction.application.usecase;

import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.CountArticleLikesQuery;
import com.socialmediablog.platform.services.interaction.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.interaction.application.command.IsArticleLikedQuery;
import com.socialmediablog.platform.services.interaction.application.command.LikeArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.RemoveBookmarkCommand;
import com.socialmediablog.platform.services.interaction.application.command.UnlikeArticleCommand;
import com.socialmediablog.platform.services.interaction.application.port.in.BookmarkArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.CountArticleLikesUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.IsArticleLikedUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.LikeArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.RemoveBookmarkUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.UnlikeArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.result.ServiceStatus;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Like;
import com.socialmediablog.platform.services.interaction.domain.repository.BookmarkRepository;
import com.socialmediablog.platform.services.interaction.domain.repository.LikeRepository;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionApplicationService implements GetServiceStatusUseCase, BookmarkArticleUseCase, RemoveBookmarkUseCase, LikeArticleUseCase, UnlikeArticleUseCase, CountArticleLikesUseCase, IsArticleLikedUseCase {

    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;

    public InteractionApplicationService(BookmarkRepository bookmarkRepository, LikeRepository likeRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.likeRepository = likeRepository;
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
    @Transactional
    public void execute(LikeArticleCommand command) {
        Instant now = Instant.now();
        InteractorId userId = InteractorId.of(command.userId());
        ArticleId articleId = ArticleId.of(command.articleId());

        likeRepository.findById(userId, articleId)
                .ifPresentOrElse(
                        like -> {},
                        () -> likeRepository.save(Like.create(userId, articleId, now))
                );
    }

    @Override
    @Transactional
    public void execute(UnlikeArticleCommand command) {
        InteractorId userId = InteractorId.of(command.userId());
        ArticleId articleId = ArticleId.of(command.articleId());

        likeRepository.findById(userId, articleId)
                .ifPresent(likeRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public long execute(CountArticleLikesQuery query) {
        return likeRepository.countByArticle(ArticleId.of(query.articleId()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean execute(IsArticleLikedQuery query) {
        return likeRepository.findById(InteractorId.of(query.userId()), ArticleId.of(query.articleId())).isPresent();
    }
}
