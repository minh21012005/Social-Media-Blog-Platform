DELETE FROM comment_claps
WHERE NOT EXISTS (
    SELECT 1 FROM comments WHERE comments.id = comment_claps.comment_id
);

DELETE FROM comment_claps
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY comment_id, user_id
                   ORDER BY created_at ASC, id ASC
               ) AS duplicate_rank
        FROM comment_claps
    ) ranked_claps
    WHERE duplicate_rank > 1
);

UPDATE comment_stats
SET clap_count = (
        SELECT COUNT(*)
        FROM comment_claps
        WHERE comment_claps.comment_id = comment_stats.comment_id
    ),
    updated_at = CURRENT_TIMESTAMP;

ALTER TABLE comment_claps
    ADD CONSTRAINT uk_comment_claps_comment_user UNIQUE (comment_id, user_id);

ALTER TABLE comment_claps
    ADD CONSTRAINT fk_comment_claps_comment
        FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE;

CREATE INDEX idx_outbox_events_cleanup ON outbox_events(status, published_at);