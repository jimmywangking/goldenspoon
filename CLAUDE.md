# CRM + 3D 模块化住房管理系统 - Agent 指南

## 项目结构

```
Downloads/AI/20260813D/
├── crm-user-auth/          # 用户认证管理子模块
│   ├── backend/            # Spring Boot 3.2.5 + MyBatis-Plus + PostgreSQL
│   └── frontend/           # Vue 3 + Vite + Element Plus + Pinia
├── crm-user-auth-test-manual.md  # 测试手册
└── docs-fix/               # 文档目录
```

## 技术栈

- **后端**: Spring Boot 3.2.5, MyBatis-Plus, PostgreSQL, Spring Security, JWT (jjwt 0.12.5), Flyway
- **前端**: Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router
- **数据库**: Docker PostgreSQL (容器名 pg-local, 端口 5432)
- **连接**: `jdbc:postgresql://localhost:5432/crm_user_auth`, user: postgres, pass: MyPass123456

## 角色权限体系

| 角色 | 说明 | 可见范围 |
|------|------|---------|
| ADMIN | 系统管理员 | 全部组织、全部用户、所有页面、角色管理 |
| ORG_ADMIN | 组织管理员 | 自己组织的用户、所有业务页面 |
| USER | 普通用户 | 按 page_permission 表授权的页面 |

## 常用命令

### 启动服务
```bash
# 后端
cd crm-user-auth/backend && mvn clean package -DskipTests -q && java -jar target/crm-user-auth-1.0.0-SNAPSHOT.jar > /tmp/backend.log 2>&1 &

# 前端
cd crm-user-auth/frontend && nohup npm run dev > /tmp/frontend.log 2>&1 &

# Docker PostgreSQL（如未运行）
open -a Docker
```

### 重启服务
```bash
pkill -f crm-user-auth; pkill -f "node.*vite"
# 然后重新执行启动命令
```

### 数据库操作
```bash
docker exec -it pg-local psql -U postgres -d crm_user_auth
```

### 查看日志
```bash
tail -f /tmp/backend.log
tail -f /tmp/frontend.log
```

## 重要注意事项

1. **Flyway 迁移**: 每次新增表/字段必须创建 Vn__xxx.sql 迁移脚本，不能直接改表
2. **用户上下文**: 使用 `UserContext` (ThreadLocal) 获取当前请求的用户信息
3. **权限校验**: Controller 层必须校验 `UserContext.isAdmin()` / `isOrgAdmin()`
4. **组织过滤**: ORG_ADMIN 操作时必须按 `UserContext.getOrgId()` 过滤数据
5. **前端路由**: 登录成功后先跳 `/` 展示主页，由菜单导航到子页面
6. **分页**: 使用 MyBatis-Plus 的 `Page<S>` 和 `IPage<S>`，注意类型转换

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |
| orgadmin | test123456 | ORG_ADMIN (org_id=3) |
| user1 | test123456 | USER (org_id=3) |

## 当前运行状态

- 后端: http://localhost:8080
- 前端: http://localhost:5173
- 数据库: Docker pg-local (postgres:15)
