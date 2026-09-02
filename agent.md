# 注意事项

- 每次改动前必须先做 plan（`/plan`），plan 中必须包含升级步骤和回退步骤。
- 代码改动必须同步编写或更新单元测试，测试全部通过后才能 commit。
- 手动验证通过后提交代码并 push 到 GitHub，push 成功后自动更新所有相关文档。
- **严禁删除现有功能来新增需求**——用户明确反对"要什么功能就删什么功能"的做法，必须在保留原有功能的前提下扩展。
- 新增数据库表/字段必须创建 Flyway 迁移脚本（Vn__xxx.sql），禁止直接改表或跳过迁移。

---

## 本仓库的执行方式（如何让上面几条落地）

### 0. Git 仓库

当前项目根目录 `/Users/helloworld/Downloads/AI/20260813D` **不是 Git 仓库**，子目录也没有 `.git`。
如需 Git 管理，请用户在项目根目录执行 `git init`，之后再遵循提交规范。

### 1. 子模块范围

```
crm-user-auth/backend/   # Spring Boot + MyBatis-Plus + PostgreSQL
crm-user-auth/frontend/  # Vue 3 + TypeScript + Vite
```

每次改动后，以实际改动的子目录为准做 commit，不要跨模块打包。

### 2. 后端测试与验证

```bash
cd /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/backend

# 编译检查（快速）
mvn compile -q

# 全量测试（含 JUnit5）
mvn test -q

# 打包（跳过测试，用于部署验证）
mvn clean package -DskipTests -q

# 启动后端
java -jar target/crm-user-auth-1.0.0-SNAPSHOT.jar > /tmp/backend.log 2>&1 &

# 查看后端日志
tail -f /tmp/backend.log
```

**新增 Service 逻辑必须同步新增单元测试**，无测试的改动视为未完成。

### 3. 前端测试与验证

```bash
cd /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/frontend

# 类型检查（严格，任何 TS 报错都会阻断构建）
npm run build   # vue-tsc + vite build，全量检查

# 开发模式（热更新，适合调试）
npm run dev
```

前端改动后必须通过 `npm run build` 类型检查才算完成，**不允许 TS 报错上线**。

### 4. Flyway 迁移规范

```
backend/src/main/resources/db/migration/
  V1__init_schema.sql
  V2__add_org_admin.sql
  V3__add_role_management.sql
  V4__add_user_page_content.sql
  V5__add_deleted_fields.sql
  V6__add_user_page_instance.sql
  V7__<下一步>            ← 新增时必须按顺序递增
```

- 迁移编号必须连续递增，不能跳号、不能重复。
- 每次 `ALTER TABLE` / `CREATE TABLE` 都必须有对应迁移文件。
- 实体类加了 `@TableField(exist = false)` 等字段但表里没有对应列，会导致运行时 SQL 错误，必须检查实体与迁移的一致性。
- 当前 PostgreSQL 容器名 `pg-local`，端口 5432，连接：`jdbc:postgresql://localhost:5432/crm_user_auth`，用户 `postgres`，密码 `MyPass123456`。

### 5. 角色权限模型（改动权限相关时必须遵守）

| 角色 | 说明 | 权限来源 |
|------|------|---------|
| ADMIN | 系统管理员 | 默认拥有所有页面，无需 permission 表记录 |
| ORG_ADMIN | 组织管理员 | 只能看自己组织的数据，所有页面默认可访问 |
| USER | 普通用户 | 从 `user_page_permission` 表动态读取 |

关键规则：
- Controller 层必须校验 `UserContext.isAdmin()` / `isOrgAdmin()`
- ORG_ADMIN 操作时必须按 `UserContext.getOrgId()` 过滤数据
- 权限加载在 `AuthService.buildUserInfo()` 中统一处理，不要在各 Controller 里手动拼

### 6. 测试账号

| 用户名 | 密码 | 角色 | org_id |
|--------|------|------|--------|
| admin | admin123 | ADMIN | null |
| orgadmin | test123456 | ORG_ADMIN | 3 |
| user1 | test123456 | USER | 3 |
