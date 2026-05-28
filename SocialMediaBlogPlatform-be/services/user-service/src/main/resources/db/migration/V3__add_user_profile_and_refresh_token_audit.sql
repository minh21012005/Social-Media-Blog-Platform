ALTER TABLE app_users
    ADD COLUMN bio VARCHAR(500),
    ADD COLUMN avatar_url VARCHAR(2048);

ALTER TABLE refresh_tokens
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

UPDATE refresh_tokens
SET updated_at = created_at
WHERE updated_at IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN updated_at SET NOT NULL;
