-- V1: Initial schema for crm-user-auth module

-- org (organization)
CREATE TABLE org (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    contact_name    VARCHAR(50),
    contact_phone   VARCHAR(20),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN NOT NULL DEFAULT false,
    deleted_at      TIMESTAMPTZ
);

COMMENT ON TABLE org IS '组织表';

CREATE INDEX idx_org_is_deleted ON org(is_deleted) WHERE is_deleted = false;

-- sys_user (user table, avoid SQL reserved word 'user')
CREATE TABLE sys_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) UNIQUE NOT NULL,
    password        VARCHAR(255) NOT NULL,
    email           VARCHAR(100),
    phone           VARCHAR(20),
    real_name       VARCHAR(50),
    org_id          BIGINT REFERENCES org(id),
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES sys_user(id),
    is_deleted      BOOLEAN NOT NULL DEFAULT false,
    deleted_at      TIMESTAMPTZ
);

COMMENT ON TABLE sys_user IS '用户表';

CREATE INDEX idx_sys_user_org_id ON sys_user(org_id) WHERE is_deleted = false;
CREATE INDEX idx_sys_user_username ON sys_user(username) WHERE is_deleted = false;
CREATE INDEX idx_sys_user_role ON sys_user(role) WHERE is_deleted = false;

-- user_page_permission
CREATE TABLE user_page_permission (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES sys_user(id),
    page_code       VARCHAR(50) NOT NULL,
    can_view        BOOLEAN NOT NULL DEFAULT true,
    can_edit        BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      BIGINT REFERENCES sys_user(id),
    is_deleted      BOOLEAN NOT NULL DEFAULT false,
    deleted_at      TIMESTAMPTZ
);

COMMENT ON TABLE user_page_permission IS '用户页面权限表';

CREATE UNIQUE INDEX uq_user_page ON user_page_permission(user_id, page_code) WHERE is_deleted = false;

-- audit_log
CREATE TABLE audit_log (
    id              BIGSERIAL PRIMARY KEY,
    operator_id     BIGINT REFERENCES sys_user(id),
    action          VARCHAR(100) NOT NULL,
    target_type     VARCHAR(50),
    target_id       BIGINT,
    detail          TEXT,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE audit_log IS '操作日志表';

CREATE INDEX idx_audit_operator ON audit_log(operator_id);
CREATE INDEX idx_audit_created_at ON audit_log(created_at DESC);

-- Insert default admin user (password: admin123, BCrypt encoded)
INSERT INTO sys_user (username, password, real_name, role, is_active, created_by)
VALUES ('admin', '$2a$12$ZDUtTqXRRYWRwcL6PF2ZWOFIwoECn4xYQCQ07QUHb5F/jVEqE4iKq', '系统管理员', 'ADMIN', true, NULL);

-- Insert demo organization
INSERT INTO org (name, contact_name, contact_phone, is_active)
VALUES ('示例组织', '张三', '13800138000', true);

-- Grant all permissions to admin
INSERT INTO user_page_permission (user_id, page_code, can_view, can_edit, created_by)
VALUES (1, 'PAGE_1', true, true, NULL),
       (1, 'PAGE_2', true, true, NULL);
