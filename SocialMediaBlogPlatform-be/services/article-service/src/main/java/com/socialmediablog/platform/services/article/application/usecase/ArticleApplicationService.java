package com.socialmediablog.platform.services.article.application.usecase;

import com.socialmediablog.platform.services.article.application.command.ArticleActionCommand;
import com.socialmediablog.platform.services.article.application.command.CreateArticleCommand;
import com.socialmediablog.platform.services.article.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.article.application.command.ListMyArticlesCommand;
import com.socialmediablog.platform.services.article.application.command.ListPublishedArticlesCommand;
import com.socialmediablog.platform.services.article.application.command.RecordArticleViewCommand;
import com.socialmediablog.platform.services.article.application.command.UpdateArticleCommand;
import com.socialmediablog.platform.services.article.application.command.UploadArticleMediaCommand;
import com.socialmediablog.platform.services.article.application.exception.ArticleNotFoundException;
import com.socialmediablog.platform.services.article.application.exception.DuplicateArticleSlugException;
import com.socialmediablog.platform.services.article.application.exception.ForbiddenArticleActionException;
import com.socialmediablog.platform.services.article.application.port.in.ArchiveArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.CreateArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.GetArticleBySlugUseCase;
import com.socialmediablog.platform.services.article.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.article.application.port.in.ListMyArticlesUseCase;
import com.socialmediablog.platform.services.article.application.port.in.ListPublishedArticlesUseCase;
import com.socialmediablog.platform.services.article.application.port.in.PublishArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.RecordArticleViewUseCase;
import com.socialmediablog.platform.services.article.application.port.in.UpdateArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.UploadArticleMediaUseCase;
import com.socialmediablog.platform.services.article.application.port.out.ArticleEventPublisher;
import com.socialmediablog.platform.services.article.application.port.out.ArticleMediaStorage;
import com.socialmediablog.platform.services.article.application.result.ArticleStatsView;
import com.socialmediablog.platform.services.article.application.result.ArticleView;
import com.socialmediablog.platform.services.article.application.result.PageResult;
import com.socialmediablog.platform.services.article.application.result.ServiceStatus;
import com.socialmediablog.platform.services.article.application.result.StoredArticleMedia;
import com.socialmediablog.platform.services.article.application.result.UploadedArticleMedia;
import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.aggregate.ArticleMediaAsset;
import com.socialmediablog.platform.services.article.domain.aggregate.ArticleRevision;
import com.socialmediablog.platform.services.article.domain.aggregate.ArticleStats;
import com.socialmediablog.platform.services.article.domain.event.ArticleArchivedEvent;
import com.socialmediablog.platform.services.article.domain.event.ArticleDraftCreatedEvent;
import com.socialmediablog.platform.services.article.domain.event.ArticlePublishedEvent;
import com.socialmediablog.platform.services.article.domain.event.ArticleUpdatedEvent;
import com.socialmediablog.platform.services.article.domain.model.ArticleCategory;
import com.socialmediablog.platform.services.article.domain.model.ArticleMediaType;
import com.socialmediablog.platform.services.article.domain.model.ArticleStatus;
import com.socialmediablog.platform.services.article.domain.repository.ArticleMediaAssetRepository;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRepository;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRevisionRepository;
import com.socialmediablog.platform.services.article.domain.repository.ArticleStatsRepository;
import com.socialmediablog.platform.services.article.domain.repository.ArticleViewRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.ArticleTitle;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleApplicationService implements
        GetServiceStatusUseCase,
        CreateArticleUseCase,
        UpdateArticleUseCase,
        PublishArticleUseCase,
        ArchiveArticleUseCase,
        GetArticleBySlugUseCase,
        ListPublishedArticlesUseCase,
        ListMyArticlesUseCase,
        UploadArticleMediaUseCase,
        RecordArticleViewUseCase {

    private static final int MAX_PAGE_SIZE = 50;

    private final ArticleRepository articleRepository;
    private final ArticleStatsRepository articleStatsRepository;
    private final ArticleRevisionRepository articleRevisionRepository;
    private final ArticleViewRepository articleViewRepository;
    private final ArticleMediaAssetRepository articleMediaAssetRepository;
    private final ArticleMediaStorage articleMediaStorage;
    private final ArticleEventPublisher articleEventPublisher;
    private final Clock clock;

    public ArticleApplicationService(
            ArticleRepository articleRepository,
            ArticleStatsRepository articleStatsRepository,
            ArticleRevisionRepository articleRevisionRepository,
            ArticleViewRepository articleViewRepository,
            ArticleMediaAssetRepository articleMediaAssetRepository,
            ArticleMediaStorage articleMediaStorage,
            ArticleEventPublisher articleEventPublisher,
            Clock clock
    ) {
        this.articleRepository = articleRepository;
        this.articleStatsRepository = articleStatsRepository;
        this.articleRevisionRepository = articleRevisionRepository;
        this.articleViewRepository = articleViewRepository;
        this.articleMediaAssetRepository = articleMediaAssetRepository;
        this.articleMediaStorage = articleMediaStorage;
        this.articleEventPublisher = articleEventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceStatus execute(GetServiceStatusCommand command) {
        return new ServiceStatus("article-service", "articles", command.currentUserId());
    }

    @Override
    @Transactional
    public ArticleView execute(CreateArticleCommand command) {
        Instant now = clock.instant();
        Slug slug = Slug.of(command.slug());
        if (articleRepository.existsBySlug(slug)) {
            throw new DuplicateArticleSlugException("Article slug is already taken");
        }
        Article article = Article.draft(
                AuthorId.of(command.authorId()),
                ArticleTitle.of(command.title()),
                slug,
                ArticleCategory.fromSlug(command.category()),
                command.summary(),
                command.content(),
                command.coverImageUrl(),
                command.tags(),
                now
        );
        Article savedArticle = articleRepository.save(article);
        articleStatsRepository.save(ArticleStats.empty(savedArticle.id(), now));
        articleEventPublisher.publish(savedArticle.id().value(), new ArticleDraftCreatedEvent(
                UUID.randomUUID(),
                savedArticle.id().value(),
                savedArticle.authorId().value(),
                now
        ));
        return view(savedArticle);
    }

    @Override
    @Transactional
    public ArticleView execute(UpdateArticleCommand command) {
        Instant now = clock.instant();
        Article existingArticle = findRequired(command.articleId());
        AuthorId actorId = AuthorId.of(command.actorId());
        try {
            existingArticle.ensureOwner(actorId);
        } catch (IllegalStateException exception) {
            throw new ForbiddenArticleActionException(exception.getMessage());
        }
        Slug slug = Slug.of(command.slug());
        if (articleRepository.existsBySlugAndIdNot(slug, existingArticle.id())) {
            throw new DuplicateArticleSlugException("Article slug is already taken");
        }
        articleRevisionRepository.save(ArticleRevision.snapshot(
                existingArticle,
                articleRevisionRepository.nextVersionFor(existingArticle.id()),
                actorId,
                now
        ));
        Article updatedArticle = articleRepository.save(existingArticle.update(
                actorId,
                ArticleTitle.of(command.title()),
                slug,
                ArticleCategory.fromSlug(command.category()),
                command.summary(),
                command.content(),
                command.coverImageUrl(),
                command.tags(),
                now
        ));
        articleEventPublisher.publish(updatedArticle.id().value(), new ArticleUpdatedEvent(
                UUID.randomUUID(),
                updatedArticle.id().value(),
                updatedArticle.authorId().value(),
                now
        ));
        return view(updatedArticle);
    }

    @Override
    @Transactional
    public ArticleView publish(ArticleActionCommand command) {
        Instant now = clock.instant();
        Article article = findRequired(command.articleId());
        AuthorId actorId = AuthorId.of(command.actorId());
        ensureOwner(article, actorId);
        Article published = articleRepository.save(article.publish(actorId, now));
        articleEventPublisher.publish(published.id().value(), new ArticlePublishedEvent(
                UUID.randomUUID(),
                published.id().value(),
                published.authorId().value(),
                now
        ));
        return view(published);
    }

    @Override
    @Transactional
    public ArticleView archive(ArticleActionCommand command) {
        Instant now = clock.instant();
        Article article = findRequired(command.articleId());
        AuthorId actorId = AuthorId.of(command.actorId());
        ensureOwner(article, actorId);
        Article archived = articleRepository.save(article.archive(actorId, now));
        articleEventPublisher.publish(archived.id().value(), new ArticleArchivedEvent(
                UUID.randomUUID(),
                archived.id().value(),
                archived.authorId().value(),
                now
        ));
        return view(archived);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleView executeBySlug(String slug) {
        Article article = articleRepository.findBySlug(Slug.of(slug))
                .filter(Article::isPublished)
                .orElseThrow(() -> new ArticleNotFoundException("Article was not found"));
        return view(article);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ArticleView> execute(ListPublishedArticlesCommand command) {
        int page = page(command.page());
        int size = size(command.size());
        ArticleCategory category = optionalCategory(command.category());
        List<Article> articles = articleRepository.findPublished(
                category,
                command.authorId(),
                command.tag(),
                command.query(),
                page,
                size
        );
        long totalItems = articleRepository.countPublished(category, command.authorId(), command.tag(), command.query());
        return PageResult.of(articles.stream().map(this::view).toList(), page, size, totalItems);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ArticleView> execute(ListMyArticlesCommand command) {
        int page = page(command.page());
        int size = size(command.size());
        ArticleStatus status = optionalStatus(command.status());
        AuthorId authorId = AuthorId.of(command.authorId());
        List<Article> articles = articleRepository.findByAuthor(authorId, status, page, size);
        long totalItems = articleRepository.countByAuthor(authorId, status);
        return PageResult.of(articles.stream().map(this::view).toList(), page, size, totalItems);
    }

    @Override
    @Transactional
    public UploadedArticleMedia execute(UploadArticleMediaCommand command) {
        validateMedia(command.content(), command.mimeType());
        StoredArticleMedia storedMedia = articleMediaStorage.upload(
                command.originalFilename(),
                command.mimeType(),
                command.content()
        );
        ArticleMediaAsset mediaAsset = articleMediaAssetRepository.save(ArticleMediaAsset.uploaded(
                null,
                command.ownerId(),
                ArticleMediaType.CONTENT_IMAGE,
                storedMedia.providerPublicId(),
                storedMedia.secureUrl(),
                storedMedia.originalFilename(),
                storedMedia.mimeType(),
                storedMedia.sizeBytes(),
                storedMedia.width(),
                storedMedia.height(),
                clock.instant()
        ));
        return new UploadedArticleMedia(
                mediaAsset.id().value(),
                mediaAsset.secureUrl(),
                mediaAsset.providerPublicId(),
                mediaAsset.mimeType(),
                mediaAsset.sizeBytes(),
                mediaAsset.width(),
                mediaAsset.height()
        );
    }

    @Override
    @Transactional
    public void execute(RecordArticleViewCommand command) {
        Article article = findRequired(command.articleId());
        if (!article.isPublished()) {
            throw new ArticleNotFoundException("Article was not found");
        }
        Instant now = clock.instant();
        articleViewRepository.save(com.socialmediablog.platform.services.article.domain.aggregate.ArticleView.record(
                article.id(),
                command.viewerId(),
                command.anonymousViewerKey(),
                command.source(),
                now
        ));
        ArticleStats stats = articleStatsRepository.findByArticleId(article.id())
                .orElseGet(() -> ArticleStats.empty(article.id(), now));
        articleStatsRepository.save(stats.recordView(now));
    }

    private Article findRequired(UUID articleId) {
        return articleRepository.findById(ArticleId.of(articleId))
                .orElseThrow(() -> new ArticleNotFoundException("Article was not found"));
    }

    private void ensureOwner(Article article, AuthorId actorId) {
        try {
            article.ensureOwner(actorId);
        } catch (IllegalStateException exception) {
            throw new ForbiddenArticleActionException(exception.getMessage());
        }
    }

    private ArticleView view(Article article) {
        ArticleStatsView stats = articleStatsRepository.findByArticleId(article.id())
                .map(ArticleStatsView::from)
                .orElseGet(ArticleStatsView::empty);
        return ArticleView.from(article, stats);
    }

    private ArticleCategory optionalCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return ArticleCategory.fromSlug(category);
    }

    private ArticleStatus optionalStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return ArticleStatus.valueOf(status.trim().toUpperCase());
    }

    private int page(int page) {
        return Math.max(page, 0);
    }

    private int size(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private void validateMedia(byte[] content, String mimeType) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Article media file is required");
        }
        if (content.length > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Article media file must not exceed 10MB");
        }
        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new IllegalArgumentException("Article media file must be an image");
        }
    }
}
