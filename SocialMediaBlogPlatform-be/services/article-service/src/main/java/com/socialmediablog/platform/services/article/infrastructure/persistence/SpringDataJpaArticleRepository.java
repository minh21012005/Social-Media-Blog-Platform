package com.socialmediablog.platform.services.article.infrastructure.persistence;

import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJpaArticleRepository extends JpaRepository<JpaArticleEntity, UUID>, JpaSpecificationExecutor<JpaArticleEntity> {

    Optional<JpaArticleEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Query(
            value = """
                    SELECT a.* FROM articles a
                    LEFT JOIN article_stats s ON s.article_id = a.id
                    WHERE a.status = 'PUBLISHED'
                      AND (:category IS NULL OR a.category = :category)
                      AND (:authorId IS NULL OR a.author_id = :authorId)
                      AND (:tag IS NULL OR EXISTS (
                          SELECT 1 FROM article_tags t
                          WHERE t.article_id = a.id AND t.tag = :tag
                      ))
                      AND (:query IS NULL OR (
                          LOWER(a.title) LIKE CONCAT('%', :query, '%')
                          OR LOWER(COALESCE(a.summary, '')) LIKE CONCAT('%', :query, '%')
                          OR LOWER(a.content) LIKE CONCAT('%', :query, '%')
                          OR EXISTS (
                              SELECT 1 FROM article_tags t
                              WHERE t.article_id = a.id AND LOWER(t.tag) LIKE CONCAT('%', :query, '%')
                          )
                      ))
                    ORDER BY
                      CASE WHEN :sort = 'views' THEN COALESCE(s.view_count, 0) ELSE 0 END DESC,
                      CASE WHEN :sort = 'popular' THEN COALESCE(s.clap_count, 0) ELSE 0 END DESC,
                      CASE WHEN :sort = 'popular' THEN COALESCE(s.view_count, 0) ELSE 0 END DESC,
                      a.published_at DESC,
                      a.created_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM articles a
                    WHERE a.status = 'PUBLISHED'
                      AND (:category IS NULL OR a.category = :category)
                      AND (:authorId IS NULL OR a.author_id = :authorId)
                      AND (:tag IS NULL OR EXISTS (
                          SELECT 1 FROM article_tags t
                          WHERE t.article_id = a.id AND t.tag = :tag
                      ))
                      AND (:query IS NULL OR (
                          LOWER(a.title) LIKE CONCAT('%', :query, '%')
                          OR LOWER(COALESCE(a.summary, '')) LIKE CONCAT('%', :query, '%')
                          OR LOWER(a.content) LIKE CONCAT('%', :query, '%')
                          OR EXISTS (
                              SELECT 1 FROM article_tags t
                              WHERE t.article_id = a.id AND LOWER(t.tag) LIKE CONCAT('%', :query, '%')
                          )
                      ))
                    """,
            nativeQuery = true
    )
    Page<JpaArticleEntity> findPublished(
            @Param("category") String category,
            @Param("authorId") UUID authorId,
            @Param("tag") String tag,
            @Param("query") String query,
            @Param("sort") String sort,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT * FROM articles
                    WHERE status = 'PUBLISHED' AND featured_rank IS NOT NULL
                    ORDER BY featured_rank ASC, published_at DESC, created_at DESC
                    """,
            nativeQuery = true
    )
    Page<JpaArticleEntity> findFeatured(Pageable pageable);

    @Query(
            value = """
                    SELECT * FROM articles
                    WHERE status = 'PUBLISHED' AND editor_pick_rank IS NOT NULL
                    ORDER BY editor_pick_rank ASC, published_at DESC, created_at DESC
                    """,
            nativeQuery = true
    )
    Page<JpaArticleEntity> findEditorPicks(Pageable pageable);

    Page<JpaArticleEntity> findByStatusAndPublishedAtGreaterThanEqual(String status, Instant publishedAt, Pageable pageable);
}
