-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    code            VARCHAR(50) NOT NULL UNIQUE,
    description     VARCHAR(200),
    is_system       BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 角色页面权限表
CREATE TABLE IF NOT EXISTS role_page_permission (
    id              BIGSERIAL PRIMARY KEY,
    role_id         BIGINT NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    page_code       VARCHAR(50) NOT NULL,
    can_view        BOOLEAN NOT NULL DEFAULT true,
    can_edit        BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (role_id, page_code)
);

-- 初始化系统角色
INSERT INTO role (name, code, description, is_system) VALUES
('系统管理员', 'ADMIN', '拥有所有权限', true),
('组织管理员', 'ORG_ADMIN', '管理本组织用户和页面', true),
('普通用户',   'USER',   '按权限访问业务页面', true);

-- 给 ADMIN 授予全部页面权限
INSERT INTO role_page_permission (role_id, page_code, can_view, can_edit)
SELECT 1, p.page_code, true, true
FROM (VALUES ('PAGE_1'), ('PAGE_2')) AS p(page_code)
ON CONFLICT DO NOTHING;

-- 给 ORG_ADMIN 授予全部页面查看+编辑权限
INSERT INTO role_page_permission (role_id, page_code, can_view, can_edit)
SELECT 2, p.page_code, true, true
FROM (VALUES ('PAGE_1'), ('PAGE_2')) AS p(page_code)
ON CONFLICT DO NOTHING;

-- 给 USER 默认只有 PAGE_1 查看权限（具体由管理员配置）
INSERT INTO role_page_permission (role_id, page_code, can_view, can_edit)
VALUES (3, 'PAGE_1', true, false),
       (3, 'PAGE_2', true, false)
ON CONFLICT DO NOTHING;
