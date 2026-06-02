package com.socialmediablog.platform.services.interaction.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Bookmark;
import com.socialmediablog.platform.services.interaction.domain.vo.ArticleId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaBookmarkEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bookmark_persistence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookmarkPersistenceTests {

    private static final Instant NOW = Instant.parse("2026-05-28T00:00:00Z");

    @Autowired
    private SpringDataJpaBookmarkRepository repository;

    @Test
    void mapsBookmarkDomainAndJpaBothWays() {
        Bookmark bookmark = Bookmark.create(
                InteractorId.of(UUID.randomUUID()),
                ArticleId.of(UUID.randomUUID()),
                NOW
        );

        Bookmark mapped = JpaBookmarkEntity.fromDomain(bookmark).toDomain();

        assertThat(mapped.userId()).isEqualTo(bookmark.userId());
        assertThat(mapped.articleId()).isEqualTo(bookmark.articleId());
        assertThat(mapped.status()).isEqualTo(bookmark.status());
        assertThat(mapped.bookmarkedAt()).isEqualTo(bookmark.bookmarkedAt());
    }

    @Test
    void rejectsDuplicateUserArticleBookmark() {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        repository.saveAndFlush(JpaBookmarkEntity.fromDomain(bookmark(userId, articleId)));

        assertThatThrownBy(() -> repository.saveAndFlush(JpaBookmarkEntity.fromDomain(bookmark(userId, articleId))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Bookmark bookmark(UUID userId, UUID articleId) {
        return Bookmark.create(
                InteractorId.of(userId),
                ArticleId.of(articleId),
                NOW
        );
    }
}
