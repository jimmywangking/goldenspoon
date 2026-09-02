CREATE TABLE IF NOT EXISTS user_page_content (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    page_code       VARCHAR(50) NOT NULL,
    content         TEXT,
    updated_by      BIGINT REFERENCES sys_user(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, page_code)
);
