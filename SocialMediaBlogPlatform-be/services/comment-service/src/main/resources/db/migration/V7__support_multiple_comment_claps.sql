ALTER TABLE comment_claps
    ADD COLUMN clap_count INTEGER NOT NULL DEFAULT 1;

ALTER TABLE comment_claps
    ADD COLUMN last_clapped_at TIMESTAMP WITH TIME ZONE;

UPDATE comment_claps
SET last_clapped_at = created_at
WHERE last_clapped_at IS NULL;

ALTER TABLE comment_claps
    ALTER COLUMN last_clapped_at SET NOT NULL;

ALTER TABLE comment_claps
    ADD CONSTRAINT chk_comment_claps_count CHECK (clap_count > 0);

UPDATE comment_stats
SET clap_count = COALESCE((
        SELECT SUM(comment_claps.clap_count)
        FROM comment_claps
        WHERE comment_claps.comment_id = comment_stats.comment_id
    ), 0),
    updated_at = CURRENT_TIMESTAMP;
