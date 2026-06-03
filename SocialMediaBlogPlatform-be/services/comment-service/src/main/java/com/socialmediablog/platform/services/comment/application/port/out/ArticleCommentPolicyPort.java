package com.socialmediablog.platform.services.comment.application.port.out;

import com.socialmediablog.platform.services.comment.application.result.ArticleCommentPolicy;
import java.util.Optional;
import java.util.UUID;

public interface ArticleCommentPolicyPort {

    Optional<ArticleCommentPolicy> findByArticleId(UUID articleId);
}
