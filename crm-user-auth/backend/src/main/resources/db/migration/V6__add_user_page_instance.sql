CREATE TABLE IF NOT EXISTS user_page_instance (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
  page_code   VARCHAR(50) NOT NULL,
  title       VARCHAR(200),
  content     TEXT,
  sort_order  INTEGER NOT NULL DEFAULT 0,
  created_by  BIGINT REFERENCES sys_user(id),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  is_deleted  BOOLEAN NOT NULL DEFAULT false,
  deleted_at  TIMESTAMPTZ
);

CREATE UNIQUE INDEX uqi_upi_user_page_title
  ON user_page_instance(user_id, page_code, title) WHERE is_deleted = false;
CREATE INDEX idx_upi_user_id ON user_page_instance(user_id, is_deleted) WHERE is_deleted = false;
CREATE INDEX idx_upi_page_code ON user_page_instance(page_code, is_deleted) WHERE is_deleted = false;

INSERT INTO user_page_instance (user_id, page_code, content, created_by, created_at, updated_at)
SELECT user_id, page_code, content, COALESCE(updated_by, user_id), created_at, updated_at
FROM user_page_content WHERE is_deleted = false;
