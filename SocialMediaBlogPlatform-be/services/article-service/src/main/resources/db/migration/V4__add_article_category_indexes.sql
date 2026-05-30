ALTER TABLE articles ADD COLUMN category VARCHAR(30);

UPDATE articles SET category = 'design' WHERE category IS NULL;

ALTER TABLE articles ALTER COLUMN category SET NOT NULL;

CREATE INDEX idx_articles_category ON articles(category);
CREATE INDEX idx_articles_status_published_at ON articles(status, published_at DESC);
CREATE INDEX idx_articles_author_status ON articles(author_id, status);
