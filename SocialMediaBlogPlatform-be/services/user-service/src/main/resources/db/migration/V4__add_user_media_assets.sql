CREATE TABLE user_media_assets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
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
    CONSTRAINT uk_user_media_assets_provider_public_id UNIQUE (provider_public_id)
);

CREATE INDEX idx_user_media_assets_user_id ON user_media_assets(user_id);
