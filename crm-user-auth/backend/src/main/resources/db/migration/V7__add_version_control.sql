-- 添加版本字段到 user_page_content
ALTER TABLE user_page_content ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE user_page_content ADD COLUMN IF NOT EXISTS version_name VARCHAR(100);

-- 移除唯一约束（改为每个版本一条记录）
ALTER TABLE user_page_content DROP CONSTRAINT IF EXISTS user_page_content_user_id_page_code_key;

-- 添加索引加速按用户+页面查询版本历史
CREATE INDEX IF NOT EXISTS idx_upc_user_page_version ON user_page_content(user_id, page_code, version DESC);
CREATE INDEX IF NOT EXISTS idx_upc_page_code_deleted ON user_page_content(page_code, is_deleted) WHERE is_deleted = false;

-- 迁移现有数据：已有数据标记为版本 1
UPDATE user_page_content SET version = 1 WHERE version IS NULL;
