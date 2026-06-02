package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleRevision;
import com.socialmediablog.platform.services.article.domain.repository.ArticleRevisionRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleRevisionEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleRevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaArticleRevisionRepositoryAdapter implements ArticleRevisionRepository {

    private final SpringDataJpaArticleRevisionRepository repository;

    public JpaArticleRevisionRepositoryAdapter(SpringDataJpaArticleRevisionRepository repository) {
        this.repository = repository;
    }

    @Override
    public int nextVersionFor(ArticleId articleId) {
        return repository.maxVersionByArticleId(articleId.value()) + 1;
    }

    @Override
    public ArticleRevision save(ArticleRevision revision) {
        return repository.save(JpaArticleRevisionEntity.fromDomain(revision)).toDomain();
    }
}
