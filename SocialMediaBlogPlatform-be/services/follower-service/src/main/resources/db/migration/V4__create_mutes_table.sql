CREATE TABLE mutes (
    id UUID PRIMARY KEY,
    muter_id UUID NOT NULL,
    muted_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_mutes_pair UNIQUE (muter_id, muted_user_id),
    CONSTRAINT chk_mutes_not_self CHECK (muter_id <> muted_user_id)
);
CREATE INDEX idx_mutes_muter_id ON mutes(muter_id);
