package com.socialmediablog.platform.services.interaction.application.usecase;

import com.socialmediablog.platform.services.interaction.application.command.BookmarkArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.CountArticleLikesQuery;
import com.socialmediablog.platform.services.interaction.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.interaction.application.command.IsArticleLikedQuery;
import com.socialmediablog.platform.services.interaction.application.command.LikeArticleCommand;
import com.socialmediablog.platform.services.interaction.application.command.ListMyBookmarksQuery;
import com.socialmediablog.platform.services.interaction.application.command.RemoveBookmarkCommand;
import com.socialmediablog.platform.services.interaction.application.command.UnlikeArticleCommand;
import com.socialmediablog.platform.services.interaction.application.port.in.BookmarkArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.CountArticleLikesUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.IsArticleLikedUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.LikeArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.ListMyBookmarksUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.RemoveBookmarkUseCase;
import com.socialmediablog.platform.services.interaction.application.port.in.UnlikeArticleUseCase;
import com.socialmediablog.platform.services.interaction.application.result.ServiceStatus;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import com.socialmediablog.platform.services.interaction.domain.aggregate.Like;
import com.socialmediablog.platform.services.interaction.domain.event.InteractionRecordedEvent;
import com.socialmediablog.platform.services.interaction.application.port.out.InteractionEventPublisher;
import com.socialmediablog.platform.services.interaction.domain.model.BookmarkStatus;
import com.socialmediablog.platform.services.interaction.domain.repository.BookmarkRepository;
import com.socialmediablog.platform.services.interaction.domain.repository.LikeRepository;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteractionApplicationService implements GetServiceStatusUseCase, BookmarkArticleUseCase, RemoveBookmarkUseCase, LikeArticleUseCase, UnlikeArticleUseCase, CountArticleLikesUseCase, IsArticleLikedUseCase, ListMyBookmarksUseCase {

    private static final Logger log = LoggerFactory.getLogger(InteractionApplicationService.class);

    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    private final InteractionEventPublisher interactionEventPublisher;

    public InteractionApplicationService(BookmarkRepository bookmarkRepository, LikeRepository likeRepository, InteractionEventPublisher interactionEventPublisher) {
        this.bookmarkRepository = bookmarkRepository;
        this.likeRepository = likeRepository;
        this.interactionEventPublisher = interactionEventPublisher;
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
                        bookmarkRepository::save,
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
                .ifPresent(bookmarkRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> execute(ListMyBookmarksQuery query) {
        InteractorId userId = InteractorId.of(query.userId());
        return bookmarkRepository.findByUserId(userId)
                .stream()
                .filter(bookmark -> bookmark.status() == BookmarkStatus.ACTIVE)
                .map(bookmark -> bookmark.articleId().value())
                .toList();
    }

    @Override
    @Transactional
    public void execute(LikeArticleCommand command) {
        Instant now = Instant.now();
        InteractorId userId = InteractorId.of(command.userId());
        ArticleId articleId = ArticleId.of(command.articleId());

        log.info("Executing LikeArticleCommand for article {} by user {}", articleId.value(), userId.value());

        likeRepository.findById(userId, articleId)
                .ifPresentOrElse(
                        like -> {
                            log.info("User {} already liked article {}", userId.value(), articleId.value());
                        },
                        () -> {
                            log.info("User {} is liking article {}", userId.value(), articleId.value());
                            likeRepository.save(Like.create(userId, articleId, now));
                            // Publish domain event so other services (notification) can react
                            InteractionRecordedEvent event = new InteractionRecordedEvent(
                                    java.util.UUID.randomUUID(), // eventId
                                    java.util.UUID.randomUUID(), // interactionId (generated)
                                    articleId.value(), // targetId (article)
                                    userId.value(), // userId (actor)
                                    now
                            );
                            interactionEventPublisher.publish(articleId.value(), event);
                            log.info("Like saved and event published for article {} by user {}", articleId.value(), userId.value());
                        }
                );
    }

    @Override
    @Transactional
    public void execute(UnlikeArticleCommand command) {
        InteractorId userId = InteractorId.of(command.userId());
        ArticleId articleId = ArticleId.of(command.articleId());
        log.info("Executing UnlikeArticleCommand for article {} by user {}", articleId.value(), userId.value());
        likeRepository.findById(userId, articleId)
                .ifPresent(like -> {
                    log.info("Found like to delete for article {} by user {}", articleId.value(), userId.value());
                    likeRepository.delete(like);
                    log.info("Like deleted for article {} by user {}", articleId.value(), userId.value());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public long execute(CountArticleLikesQuery query) {
        long count = likeRepository.countByArticle(ArticleId.of(query.articleId()));
        log.info("Executing CountArticleLikesQuery for article {}. Count: {}", query.articleId(), count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean execute(IsArticleLikedQuery query) {
        boolean isPresent = likeRepository.findById(InteractorId.of(query.userId()), ArticleId.of(query.articleId())).isPresent();
        log.info("Executing IsArticleLikedQuery for article {} by user {}. Is present: {}", query.articleId(), query.userId(), isPresent);
        return isPresent;
    }
}
