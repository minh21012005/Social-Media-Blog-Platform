package com.socialmediablog.platform.services.article.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.vo.ArticleTitle;
import com.socialmediablog.platform.services.article.domain.vo.AuthorId;
import com.socialmediablog.platform.services.article.domain.vo.Slug;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:article_persistence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticlePersistenceTests {

    private static final Instant NOW = Instant.parse("2026-05-28T00:00:00Z");

    @Autowired
    private SpringDataJpaArticleRepository repository;

    @Test
    void mapsArticleDomainAndJpaBothWays() {
        Article article = article("clean-ddd-base");

        Article mapped = JpaArticleEntity.fromDomain(article).toDomain();

        assertThat(mapped.id()).isEqualTo(article.id());
        assertThat(mapped.authorId()).isEqualTo(article.authorId());
        assertThat(mapped.title()).isEqualTo(article.title());
        assertThat(mapped.slug()).isEqualTo(article.slug());
        assertThat(mapped.summary()).isEqualTo("Persistence mapping");
        assertThat(mapped.coverImageUrl()).isEqualTo("https://cdn.example.com/cover.png");
        assertThat(mapped.tags()).containsExactlyInAnyOrder("ddd", "spring");
    }

    @Test
    void rejectsDuplicateArticleSlug() {
        repository.saveAndFlush(JpaArticleEntity.fromDomain(article("duplicate-slug")));

        assertThatThrownBy(() -> repository.saveAndFlush(JpaArticleEntity.fromDomain(article("duplicate-slug"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Article article(String slug) {
        return Article.draft(
                AuthorId.of(UUID.randomUUID()),
                ArticleTitle.of("Clean DDD Base"),
                Slug.of(slug),
                "Persistence mapping",
                "Article content for persistence mapping.",
                "https://cdn.example.com/cover.png",
                Set.of("DDD", "Spring"),
                NOW
        );
    }
}
