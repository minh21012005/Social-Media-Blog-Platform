package com.socialmediablog.platform.services.article.application.port.out;

import com.socialmediablog.platform.services.article.application.result.StoredArticleMedia;

public interface ArticleMediaStorage {

    StoredArticleMedia upload(String originalFilename, String mimeType, byte[] content);
}
