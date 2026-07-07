CREATE TABLE IF NOT EXISTS comment_claps (
    id UUID PRIMARY KEY,
    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_comment_claps_comment_id ON comment_claps(comment_id);
