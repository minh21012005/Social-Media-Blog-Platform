package com.socialmediablog.platform.services.article.infrastructure.persistence;

import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJpaArticleRepository extends JpaRepository<JpaArticleEntity, UUID> {

    Optional<JpaArticleEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Query("""
            select distinct article from JpaArticleEntity article
            left join article.tags tag
            where article.status = 'PUBLISHED'
              and (:category is null or article.category = :category)
              and (:authorId is null or article.authorId = :authorId)
              and (:tag is null or tag = :tag)
              and (
                :query is null
                or lower(article.title) like lower(concat('%', :query, '%'))
                or lower(article.summary) like lower(concat('%', :query, '%'))
                or lower(article.content) like lower(concat('%', :query, '%'))
              )
            order by article.publishedAt desc, article.createdAt desc
            """)
    List<JpaArticleEntity> findPublished(
            @Param("category") String category,
            @Param("authorId") UUID authorId,
            @Param("tag") String tag,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("""
            select count(distinct article) from JpaArticleEntity article
            left join article.tags tag
            where article.status = 'PUBLISHED'
              and (:category is null or article.category = :category)
              and (:authorId is null or article.authorId = :authorId)
              and (:tag is null or tag = :tag)
              and (
                :query is null
                or lower(article.title) like lower(concat('%', :query, '%'))
                or lower(article.summary) like lower(concat('%', :query, '%'))
                or lower(article.content) like lower(concat('%', :query, '%'))
              )
            """)
    long countPublished(
            @Param("category") String category,
            @Param("authorId") UUID authorId,
            @Param("tag") String tag,
            @Param("query") String query
    );

    @Query("""
            select article from JpaArticleEntity article
            where article.authorId = :authorId
              and (:status is null or article.status = :status)
            order by article.updatedAt desc
            """)
    List<JpaArticleEntity> findByAuthorId(
            @Param("authorId") UUID authorId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
            select count(article) from JpaArticleEntity article
            where article.authorId = :authorId
              and (:status is null or article.status = :status)
            """)
    long countByAuthorId(@Param("authorId") UUID authorId, @Param("status") String status);
}
