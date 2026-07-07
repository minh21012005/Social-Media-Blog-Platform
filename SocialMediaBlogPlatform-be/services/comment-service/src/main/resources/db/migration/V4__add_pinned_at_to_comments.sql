ALTER TABLE comments ADD COLUMN pinned_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_comments_pinned_at ON comments(pinned_at);
