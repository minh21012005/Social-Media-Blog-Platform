package com.socialmediablog.platform.services.comment.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.socialmediablog.platform.services.comment.domain.aggregate.Comment;
import com.socialmediablog.platform.services.comment.domain.vo.ArticleId;
import com.socialmediablog.platform.services.comment.domain.vo.AuthorId;
import com.socialmediablog.platform.services.comment.domain.vo.CommentContent;
import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaCommentEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:comment_persistence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentPersistenceTests {

    private static final Instant NOW = Instant.parse("2026-05-28T00:00:00Z");

    @Autowired
    private SpringDataJpaCommentRepository repository;

    @Test
    void mapsAndPersistsOneLevelReply() {
        UUID articleId = UUID.randomUUID();
        Comment parent = Comment.create(
                ArticleId.of(articleId),
                AuthorId.of(UUID.randomUUID()),
                null,
                CommentContent.of("Parent comment"),
                NOW
        );
        Comment child = Comment.create(
                ArticleId.of(articleId),
                AuthorId.of(UUID.randomUUID()),
                parent.id(),
                CommentContent.of("Reply comment"),
                NOW
        );

        repository.saveAndFlush(JpaCommentEntity.fromDomain(parent));
        JpaCommentEntity savedChild = repository.saveAndFlush(JpaCommentEntity.fromDomain(child));

        Comment mappedChild = savedChild.toDomain();
        assertThat(mappedChild.isReply()).isTrue();
        assertThat(mappedChild.parentCommentId()).isEqualTo(parent.id());
        assertThat(repository.findByArticleId(articleId)).hasSize(2);
    }
}
