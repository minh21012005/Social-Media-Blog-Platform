CREATE INDEX idx_outbox_events_cleanup ON outbox_events(status, published_at);
