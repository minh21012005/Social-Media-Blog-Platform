package com.socialmediablog.platform.services.interaction.application.usecase;

import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.interaction.application.command.RemoveBookmarkCommand;
import com.socialmediablog.platform.services.interaction.application.port.in.BookmarkArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.RemoveBookmarkUseCase;
import com.socialmediablog.platform.services.interaction.application.result.ServiceStatus;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import com.socialmediablog.platform.services.interaction.domain.repository.BookmarkRepository;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionApplicationService implements GetServiceStatusUseCase, BookmarkArticleUseCase, RemoveBookmarkUseCase {

    private final BookmarkRepository bookmarkRepository;

    public InteractionApplicationService(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
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
}
