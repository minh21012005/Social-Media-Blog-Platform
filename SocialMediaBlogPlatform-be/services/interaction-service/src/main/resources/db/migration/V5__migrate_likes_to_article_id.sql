ALTER TABLE likes ADD COLUMN IF NOT EXISTS article_id UUID;

-- Map dữ liệu cũ (chỉ giữ likes của ARTICLE)
UPDATE likes
SET article_id = target_id
WHERE article_id IS NULL
  AND target_type = 'ARTICLE';

-- Nếu còn bản ghi không map được (vd COMMENT), loại bỏ để đảm bảo NOT NULL
DELETE FROM likes
WHERE article_id IS NULL;

ALTER TABLE likes ALTER COLUMN article_id SET NOT NULL;

-- Chuẩn hóa khóa chính theo entity mới
ALTER TABLE likes DROP CONSTRAINT IF EXISTS likes_pkey;
ALTER TABLE likes ADD CONSTRAINT likes_pkey PRIMARY KEY (user_id, article_id);

-- Dọn schema cũ
ALTER TABLE likes DROP CONSTRAINT IF EXISTS uk_like_user_target;
ALTER TABLE likes DROP CONSTRAINT IF EXISTS chk_like_target_type;
DROP INDEX IF EXISTS idx_likes_target;
ALTER TABLE likes DROP COLUMN IF EXISTS target_id;
ALTER TABLE likes DROP COLUMN IF EXISTS target_type;
ALTER TABLE likes DROP COLUMN IF EXISTS id;
