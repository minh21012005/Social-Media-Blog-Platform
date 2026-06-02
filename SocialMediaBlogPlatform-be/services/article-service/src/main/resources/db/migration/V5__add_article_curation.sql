ALTER TABLE articles ADD COLUMN featured_rank INTEGER;
ALTER TABLE articles ADD COLUMN editor_pick_rank INTEGER;

CREATE INDEX idx_articles_status_featured_rank ON articles(status, featured_rank);
CREATE INDEX idx_articles_status_editor_pick_rank ON articles(status, editor_pick_rank);
