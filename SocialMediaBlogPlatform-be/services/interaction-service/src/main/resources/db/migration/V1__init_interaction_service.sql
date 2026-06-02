-- Base schema marker for interaction-service. Feature tables will be added in later migrations.
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE likes (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id UUID NOT NULL,
                       target_id UUID NOT NULL,
                       target_type VARCHAR(30) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_like_user_target UNIQUE (user_id, target_id, target_type),
                       CONSTRAINT chk_like_target_type CHECK (target_type IN ('ARTICLE', 'COMMENT'))
);

CREATE TABLE claps (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id UUID NOT NULL,
                       article_id UUID NOT NULL,
                       clap_count INT NOT NULL DEFAULT 1,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_clap_user_article UNIQUE (user_id, article_id),
                       CONSTRAINT chk_clap_count CHECK (clap_count > 0)
);

CREATE INDEX idx_likes_target ON likes(target_type, target_id);
CREATE INDEX idx_likes_user ON likes(user_id);

CREATE INDEX idx_claps_article ON claps(article_id);
CREATE INDEX idx_claps_user ON claps(user_id);
