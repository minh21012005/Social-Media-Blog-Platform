CREATE TABLE IF NOT EXISTS likes (
    user_id UUID NOT NULL,
    article_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, article_id)
);
