package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleStats;
import com.socialmediablog.platform.services.article.domain.repository.ArticleStatsRepository;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleStatsEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleStatsRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaArticleStatsRepositoryAdapter implements ArticleStatsRepository {

    private final SpringDataJpaArticleStatsRepository repository;

    public JpaArticleStatsRepositoryAdapter(SpringDataJpaArticleStatsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ArticleStats> findByArticleId(ArticleId articleId) {
        return repository.findByArticleId(articleId.value()).map(JpaArticleStatsEntity::toDomain);
    }

    @Override
    public ArticleStats save(ArticleStats articleStats) {
        return repository.save(JpaArticleStatsEntity.fromDomain(articleStats)).toDomain();
    }
}
