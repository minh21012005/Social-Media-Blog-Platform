package com.socialmediablog.platform.services.article.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.socialmediablog.platform.services.article.application.command.CurateArticleCommand;
import com.socialmediablog.platform.services.article.application.port.out.ArticleEventPublisher;
import com.socialmediablog.platform.services.article.application.port.out.ArticleMediaStorage;
import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.model.ArticleCategory;
import com.socialmediablog.platform.services.article.domain.repository.ArticleMediaAssetRepository;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRepository;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRevisionRepository;
import com.socialmediablog.platform.services.article.domain.repository.ArticleStatsRepository;
import com.socialmediablog.platform.services.article.domain.repository.ArticleViewRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleTitle;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleApplicationServiceCurationTests {

    private static final Instant NOW = Instant.parse("2026-07-15T08:00:00Z");

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleStatsRepository articleStatsRepository;
    @Mock
    private ArticleRevisionRepository articleRevisionRepository;
    @Mock
    private ArticleViewRepository articleViewRepository;
    @Mock
    private ArticleMediaAssetRepository articleMediaAssetRepository;
    @Mock
    private ArticleMediaStorage articleMediaStorage;
    @Mock
    private ArticleEventPublisher articleEventPublisher;

    private ArticleApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ArticleApplicationService(
                articleRepository,
                articleStatsRepository,
                articleRevisionRepository,
                articleViewRepository,
                articleMediaAssetRepository,
                articleMediaStorage,
                articleEventPublisher,
                Clock.fixed(NOW, ZoneOffset.UTC));

        when(articleRepository.save(any(Article.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void curateReplacesExistingFeaturedArticleInTheSameOperation() {
        Article currentFeatured = publishedArticle("current-featured").curate(1, null, NOW);
        Article target = publishedArticle("new-featured");
        when(articleRepository.findById(target.id())).thenReturn(Optional.of(target));
        when(articleRepository.findByFeaturedRank(1)).thenReturn(Optional.of(currentFeatured));
        when(articleStatsRepository.findByArticleId(target.id())).thenReturn(Optional.empty());

        service.curate(new CurateArticleCommand(target.id().value(), 1, null));

        ArgumentCaptor<Article> savedArticles = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository, org.mockito.Mockito.times(2)).save(savedArticles.capture());
        List<Article> values = savedArticles.getAllValues();
        assertThat(values.get(0).id()).isEqualTo(currentFeatured.id());
        assertThat(values.get(0).featuredRank()).isNull();
        assertThat(values.get(1).id()).isEqualTo(target.id());
        assertThat(values.get(1).featuredRank()).isEqualTo(1);
        assertThat(values.get(1).editorPickRank()).isNull();
    }

    @Test
    void curateReplacesOnlyTheRequestedEditorPickSlot() {
        Article currentPick = publishedArticle("current-pick").curate(null, 3, NOW);
        Article target = publishedArticle("new-pick");
        when(articleRepository.findById(target.id())).thenReturn(Optional.of(target));
        when(articleRepository.findByEditorPickRank(3)).thenReturn(Optional.of(currentPick));
        when(articleStatsRepository.findByArticleId(target.id())).thenReturn(Optional.empty());

        service.curate(new CurateArticleCommand(target.id().value(), null, 3));

        ArgumentCaptor<Article> savedArticles = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository, org.mockito.Mockito.times(2)).save(savedArticles.capture());
        List<Article> values = savedArticles.getAllValues();
        assertThat(values.get(0).id()).isEqualTo(currentPick.id());
        assertThat(values.get(0).editorPickRank()).isNull();
        assertThat(values.get(1).id()).isEqualTo(target.id());
        assertThat(values.get(1).featuredRank()).isNull();
        assertThat(values.get(1).editorPickRank()).isEqualTo(3);
    }

    private Article publishedArticle(String slug) {
        AuthorId authorId = AuthorId.of(UUID.randomUUID());
        return Article.draft(
                authorId,
                ArticleTitle.of("Article " + slug),
                Slug.of(slug),
                ArticleCategory.DESIGN,
                "A concise article summary.",
                "Article content",
                "https://example.com/cover.jpg",
                Set.of("design"),
                NOW).publish(authorId, NOW);
    }
}
