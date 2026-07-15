CREATE INDEX idx_outbox_events_cleanup ON outbox_events(status, published_at);
CREATE INDEX idx_processed_events_created_at ON processed_events(created_at);
