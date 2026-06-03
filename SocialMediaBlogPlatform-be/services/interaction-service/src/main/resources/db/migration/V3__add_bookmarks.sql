CREATE TABLE bookmarks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    article_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    bookmarked_at TIMESTAMP NOT NULL,
    removed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_bookmarks_user_article UNIQUE (user_id, article_id)
);

CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_article_id ON bookmarks(article_id);



