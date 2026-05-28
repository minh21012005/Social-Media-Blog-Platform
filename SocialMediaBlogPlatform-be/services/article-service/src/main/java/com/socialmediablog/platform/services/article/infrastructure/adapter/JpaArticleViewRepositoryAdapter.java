package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleView;
import com.socialmediablog.platform.services.article.domain.repository.ArticleViewRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleViewEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleViewRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class JpaArticleViewRepositoryAdapter implements ArticleViewRepository {

    private final SpringDataJpaArticleViewRepository repository;

    public JpaArticleViewRepositoryAdapter(SpringDataJpaArticleViewRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ArticleView> findByArticleId(ArticleId articleId) {
        return repository.findByArticleId(articleId.value()).stream()
                .map(JpaArticleViewEntity::toDomain)
                .toList();
    }

    @Override
    public ArticleView save(ArticleView articleView) {
        return repository.save(JpaArticleViewEntity.fromDomain(articleView)).toDomain();
    }
}
