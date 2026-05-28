package com.socialmediablog.platform.services.article.domain.repository;

import com.socialmediablog.platform.services.article.domain.aggregate.ArticleMediaAsset;
import com.socialmediablog.platform.services.article.domain.vo.ArticleId;
import com.socialmediablog.platform.services.article.domain.vo.ArticleMediaAssetId;
import java.util.List;
import java.util.Optional;

public interface ArticleMediaAssetRepository {

    Optional<ArticleMediaAsset> findById(ArticleMediaAssetId id);

    List<ArticleMediaAsset> findByArticleId(ArticleId articleId);

    ArticleMediaAsset save(ArticleMediaAsset mediaAsset);
}
