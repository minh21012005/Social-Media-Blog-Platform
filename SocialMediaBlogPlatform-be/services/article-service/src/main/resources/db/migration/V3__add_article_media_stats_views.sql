CREATE TABLE article_stats (
    id UUID PRIMARY KEY,
    article_id UUID NOT NULL UNIQUE REFERENCES articles(id) ON DELETE CASCADE,
    clap_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    bookmark_count BIGINT NOT NULL DEFAULT 0,
    last_interaction_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE article_views (
    id UUID PRIMARY KEY,
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    viewer_id UUID,
    anonymous_viewer_key VARCHAR(120),
    source VARCHAR(80),
    viewed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE article_media_assets (
    id UUID PRIMARY KEY,
    article_id UUID REFERENCES articles(id) ON DELETE SET NULL,
    owner_id UUID NOT NULL,
    media_type VARCHAR(30) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_public_id VARCHAR(255) NOT NULL,
    secure_url VARCHAR(2048) NOT NULL,
    original_filename VARCHAR(255),
    mime_type VARCHAR(120),
    size_bytes BIGINT NOT NULL,
    width INTEGER,
    height INTEGER,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_article_media_assets_provider_public_id UNIQUE (provider_public_id)
);

CREATE INDEX idx_article_views_article_id ON article_views(article_id);
CREATE INDEX idx_article_views_viewer_id ON article_views(viewer_id);
CREATE INDEX idx_article_media_assets_article_id ON article_media_assets(article_id);
CREATE INDEX idx_article_media_assets_owner_id ON article_media_assets(owner_id);
