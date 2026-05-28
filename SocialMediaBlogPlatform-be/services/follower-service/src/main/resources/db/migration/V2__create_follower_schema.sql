CREATE TABLE follow_relations (
    id UUID PRIMARY KEY,
    follower_id UUID NOT NULL,
    followed_user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    followed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    unfollowed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_follow_relations_pair UNIQUE (follower_id, followed_user_id),
    CONSTRAINT chk_follow_relations_not_self CHECK (follower_id <> followed_user_id)
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_follow_relations_follower_id ON follow_relations(follower_id);
CREATE INDEX idx_follow_relations_followed_user_id ON follow_relations(followed_user_id);
CREATE INDEX idx_follow_relations_status ON follow_relations(status);
CREATE INDEX idx_outbox_events_status ON outbox_events(status);
