# crm-user-auth 操作手册

> 更新时间：2026-09-05
> 状态：**已完成且全量验收通过（含角色权限、页面实例、Swagger UI）✅**
> 后端：http://localhost:8080 | 前端：http://localhost:5173 | Swagger UI：http://localhost:8080/swagger-ui
> 数据库：Docker PostgreSQL（容器名 pg-local）

---

## 一、基础环境信息

### 数据库连接
```
主机：localhost:5432
库名：crm_user_auth
用户名：postgres
密码：MyPass123456
```

### 查看数据库
```bash
# 进入数据库
docker exec -it pg-local psql -U postgres -d crm_user_auth

# 查看所有表
\dt

# 退出
\q
```

### 默认账号
| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| ADMIN | admin | admin123 | 所有页面（PAGE_1 + PAGE_2 查看+编辑）、组织/用户/角色管理、页面实例管理 |
| ORG_ADMIN | orgadmin | test123456 | 所属组织（测试公司更新）的全部用户、所有业务页面、组织管理 |
| USER | user1 | test123456 | 按权限访问业务页面（默认 PAGE_1/PAGE_2 可查看） |

### 其他测试账号（密码均为 test123456）
| 用户名 | 角色 | 所属组织 |
|--------|------|---------|
| testuser | ADMIN | 测试公司更新 |
| orguser1 | USER | 测试公司更新 |
| orgmember | USER | 测试公司更新 |
| otherorg | USER | 示例组织 |
| orgmember2 | USER | 软删除测试组织 |

---

## 二、API 测试（用 curl）

### 2.0 第一步：确认登录可用（必须先做）

**所有其他 API 测试都依赖登录 Token，这一步必须通过。**

```bash
curl -s http://localhost:8080/api/auth/login \
  -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | python3 -m json.tool
```

**预期结果：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "expiresIn": 86400,
    "user": { "username": "admin", "role": "ADMIN", ... }
  }
}
```

**如果登录失败（返回 code=400），检查：**
```bash
docker exec pg-local psql -U postgres -d crm_user_auth \
  -c "SELECT password FROM sys_user WHERE username='admin' AND is_deleted=false;"
```
正确值应为：`$2a$12$7btG3/9wRTMly7X2QkzdmuoqUorGIvqg/HKEFJ3OfdrqeWC/sQyH2`

### 2.1 保存 Token 供后续使用

```bash
TOKEN=$(curl -s http://localhost:8080/api/auth/login \
  -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "TOKEN=$TOKEN"
```

---

### 2.2 认证相关 API

#### 获取当前用户信息
```bash
curl http://localhost:8080/api/auth/me -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### Token 无效/过期的错误码
| HTTP状态 | code | message |
|----------|------|---------|
| 400 | 400 | 用户名或密码错误 |
| 401 | 401 | Token无效或已过期 |
| 403 | — | 未登录访问受保护接口 |

---

### 2.3 组织管理 API

#### 查询组织列表（分页）
```bash
curl "http://localhost:8080/api/orgs?page=1&size=20" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
参数：`page`（页码，默认1）、`size`（每页条数，默认20）、`keyword`（名称模糊搜索）

#### 创建组织
```bash
curl -s -X POST http://localhost:8080/api/orgs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试公司","contactName":"李四","contactPhone":"13900139000"}' \
  | python3 -m json.tool
```

#### 查询单个组织
```bash
curl -s http://localhost:8080/api/orgs/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 更新组织
```bash
curl -s -X PUT http://localhost:8080/api/orgs/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"示例组织更新","contactName":"张三","contactPhone":"13800138000"}' \
  | python3 -m json.tool
```

#### 删除组织（软删除）
```bash
curl -s -X DELETE http://localhost:8080/api/orgs/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
> 注：软删除，is_deleted 设为 true，原数据保留。若该组织下有用户，则用户 org_id 置为 null。

---

### 2.4 用户管理 API

#### 查询用户列表
```bash
curl "http://localhost:8080/api/users?page=1&size=20&keyword=admin&role=ADMIN&orgId=1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
参数：`current`、`size`、`keyword`（用户名/姓名模糊搜索）、`role`（ADMIN/USER/ORG_ADMIN）、`orgId`（按组织筛选）

> **权限规则**：ADMIN 查看全部；ORG_ADMIN 只看自己组织；普通用户返回 403。

#### 查询单个用户详情
```bash
curl -s http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 创建用户
```bash
curl -s -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"pass123456","realName":"新用户","role":"USER","orgId":1}' \
  | python3 -m json.tool
```

#### 更新用户信息
```bash
curl -s -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"realName":"更新姓名","email":"new@example.com","phone":"13800001111","role":"USER","isActive":true}' \
  | python3 -m json.tool
```

#### 重置用户密码
```bash
curl -s -X POST http://localhost:8080/api/users/1/reset-password \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newPassword":"newpass123"}' \
  | python3 -m json.tool
```

#### 删除用户（软删除）
```bash
curl -s -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

---

### 2.5 角色管理 API（仅 ADMIN）

#### 查询角色列表
```bash
curl "http://localhost:8080/api/roles?page=1&size=20" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 创建角色
```bash
curl -s -X POST http://localhost:8080/api/roles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试角色","code":"TEST_ROLE","description":"测试描述","isSystem":false}' \
  | python3 -m json.tool
```

#### 查询单个角色
```bash
curl -s http://localhost:8080/api/roles/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 更新角色
```bash
curl -s -X PUT http://localhost:8080/api/roles/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"更新角色名","code":"TEST_ROLE","description":"更新描述","isSystem":false}' \
  | python3 -m json.tool
```

#### 删除角色（系统角色不可删除）
```bash
curl -s -X DELETE http://localhost:8080/api/roles/2 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 获取角色的页面权限
```bash
curl -s http://localhost:8080/api/roles/1/permissions \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 设置角色的页面权限
```bash
curl -s -X PUT http://localhost:8080/api/roles/1/permissions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{"pageCode":"PAGE_1"},{"pageCode":"PAGE_2"}]' \
  | python3 -m json.tool
```

---

### 2.6 页面权限 API

#### 查询用户权限
```bash
curl -s http://localhost:8080/api/users/1/permissions \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 设置用户权限
```bash
curl -s -X PUT http://localhost:8080/api/users/1/permissions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissions": [
      {"pageCode": "PAGE_1", "canView": true, "canEdit": true},
      {"pageCode": "PAGE_2", "canView": true, "canEdit": false}
    ]
  }' | python3 -m json.tool
```

---

### 2.7 页面内容 API

#### 获取当前用户的页面内容
```bash
curl -s http://localhost:8080/api/pages/PAGE_1 \
  -H "Authorization: Bearer $TOKEN"
```
返回该用户 PAGE_1 页面的 JSON 内容字符串。

#### 保存页面内容
```bash
curl -s -X PUT http://localhost:8080/api/pages/PAGE_1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"{\"widgets\":[{\"type\":\"chart\",\"x\":0,\"y\":0}]}"}'
```

#### 管理员查看所有用户的页面内容
```bash
curl -s http://localhost:8080/api/pages/PAGE_1/all \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

---

### 2.8 页面实例 API

#### 查询我的页面实例（分页）
```bash
curl "http://localhost:8080/api/instances?pageCode=PAGE_1&current=1&size=10" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 获取单个实例详情
```bash
curl -s http://localhost:8080/api/instances/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 创建页面实例
```bash
curl -s -X POST http://localhost:8080/api/instances \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pageCode":"PAGE_1","title":"我的设计","content":"{\"layout\":\"custom\"}"}' \
  | python3 -m json.tool
```
> USER 每个 pageCode 最多 5 个实例，超出返回 400。

#### 更新实例
```bash
curl -s -X PUT http://localhost:8080/api/instances/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"更新标题","content":"{\"layout\":\"new\"}"}' \
  | python3 -m json.tool
```

#### 删除实例
```bash
curl -s -X DELETE http://localhost:8080/api/instances/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 管理员查看所有实例
```bash
curl "http://localhost:8080/api/instances/admin/all?pageCode=PAGE_1&current=1&size=20" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

---

## 三、Swagger UI API 测试界面

访问 http://localhost:8080/swagger-ui

### 使用方法
1. 点击右上角 **Authorize** 按钮
2. 在 Value 输入框中粘贴完整的 JWT token（不需要加 `Bearer ` 前缀）
3. 点击 Authorize → Close
4. 展开任意接口，点击 **Try it out** → **Execute** 即可测试

### 认证方式
- 所有接口（除 login/refresh）都需要在 Header 中携带 `Authorization: Bearer <token>`
- Swagger UI 会自动将 token 添加到请求头

---

## 四、页面权限规则

### 角色说明

| 角色 | 说明 | 可见范围 |
|------|------|---------|
| ADMIN | 系统管理员 | 全部组织、全部用户、所有页面、角色管理 |
| ORG_ADMIN | 组织管理员 | 自己组织的用户、所有业务页面 |
| USER | 普通用户 | 仅拥有 PAGE_1/PAGE_2 权限的页面 |

### 权限控制流程

```
登录 → JWT 携带 user 信息（含 role、orgId、orgName）
     → 路由守卫检查 meta.requiredPermission / requiresAdmin
     → 有权限 → 进入页面（可看到编辑按钮 if canEdit）
     → 无权限 → 跳转 /403 页面
```

### 页面实例数量限制

| 角色 | 每个 pageCode 最大实例数 |
|------|----------------------|
| ADMIN | 无限制 |
| ORG_ADMIN | 20 |
| USER | 5 |

超出限制返回：
```json
{"code": 400, "message": "超出页面实例数量限制（最多5个）", "data": null}
```

---

## 五、前端页面测试

访问 http://localhost:5173

### 测试流程
1. 打开浏览器访问 http://localhost:5173
2. 登录页输入 admin / admin123
3. 登录成功后进入首页，左侧菜单可访问各管理页面

### 各页面功能

#### 组织管理（/orgs）
- 新增/编辑/删除组织
- 搜索组织名称
- 仅 ADMIN 可访问

#### 用户管理（/users）
- 新增/编辑/删除用户
- 搜索用户名/姓名
- 按角色、组织筛选
- 重置密码
- ORG_ADMIN 只能看到自己组织的用户

#### 角色管理（/roles）
- 新增/编辑/删除角色
- 配置角色可访问的页面权限
- 仅 ADMIN 可访问

#### 页面 1（/page1）
- 查看当前用户的 PAGE_1 内容
- 有编辑权限时显示编辑按钮
- 无权限时路由拦截跳转 403

#### 页面 2（/page2）
- 查看当前用户的 PAGE_2 内容
- 有编辑权限时显示编辑按钮
- 无权限时路由拦截跳转 403

#### 页面实例管理（/instances）
- 选择页面类型（PAGE_1 / PAGE_2）
- 查看当前用户的实例列表
- 新建/编辑/删除实例
- USER 每个类型最多 5 个实例
- 显示已有实例数量和上限

### 页面权限验证
1. 用 admin 登录，创建一个新用户 testuser，密码 test123456，所属组织选择"测试公司更新"，角色选 USER
2. 给用户分配 PAGE_1 权限（canView=true, canEdit=false），不分配 PAGE_2 权限
3. 退出，用 testuser/test123456 登录
4. 验证：能进入 PAGE_1（只读），尝试进入 PAGE_2 被路由拦截显示 403 页面
5. 用 admin 给 testuser 也分配 PAGE_2 编辑权限
6. 再次用 testuser 登录，验证可以进入 PAGE_2 并编辑
7. 用 orgadmin 登录，验证只能看到自己组织的用户（如"测试公司更新"下的用户）
8. 用普通用户 user1 登录，尝试访问用户管理页面，验证被拦截（403）

---

## 六、数据库操作

### 常用 SQL 查询
```bash
# 进入数据库
docker exec -it pg-local psql -U postgres -d crm_user_auth

-- 查看所有组织
SELECT id, name, is_active, is_deleted FROM org;

-- 查看所有用户
SELECT id, username, role, is_active, is_deleted, org_id FROM sys_user;

-- 查看用户权限
SELECT u.username, p.page_code, p.can_view, p.can_edit
FROM user_page_permission p
JOIN sys_user u ON p.user_id = u.id
WHERE p.is_deleted = false;

-- 查看角色及页面权限
SELECT r.name, r.code, r.is_system, r.is_deleted FROM role;
SELECT rp.role_id, rp.page_code FROM role_page_permission rp;

-- 查看页面实例
SELECT id, user_id, page_code, title, LEFT(content, 50) as content_preview
FROM user_page_instance WHERE is_deleted = false ORDER BY created_at DESC;

-- 查看操作日志
SELECT id, operator_id, action, target_type, created_at
FROM audit_log ORDER BY created_at DESC LIMIT 20;

\q
```

### 查看表结构
```bash
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d org"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d sys_user"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d role"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d role_page_permission"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d user_page_permission"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d user_page_content"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d user_page_instance"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d audit_log"
```

---

## 七、常见问题排查

### 登录返回 400 "用户名或密码错误"
```bash
# 检查数据库中 admin 密码哈希是否正确
docker exec pg-local psql -U postgres -d crm_user_auth \
  -c "SELECT password FROM sys_user WHERE username='admin' AND is_deleted=false;"
# 正确值应为 $2a$12$7btG3/9wRTMly7X2QkzdmuoqUorGIvqg/HKEFJ3OfdrqeWC/sQyH2
```

### 端口被占用
```bash
lsof -i:8080
kill <PID>
```

### 数据库连接失败
```bash
docker ps | grep pg-local
docker restart pg-local
```

### Swagger UI 没有 Authorize 按钮
- 确保浏览器没有缓存旧版本，按 Ctrl+Shift+R 强制刷新
- 或清除浏览器缓存后重新访问 http://localhost:8080/swagger-ui

### 页面实例数量限制
- 创建第 6 个实例时返回 400，提示"超出页面实例数量限制"
- ORG_ADMIN 每个 pageCode 最多 20 个，ADMIN 无限制

---

## 八、后端启动/停止

```bash
# 启动
java -jar /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/backend/target/crm-user-auth-1.0.0-SNAPSHOT.jar > /tmp/backend.log 2>&1 &

# 停止
pkill -f crm-user-auth

# 查看日志
tail -f /tmp/backend.log

# 重新编译
cd /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/backend && mvn clean package -DskipTests -q
```

## 九、前端启动/停止

```bash
# 启动
cd /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/frontend && npm run dev

# 构建
npm run build

# 停止
Ctrl+C（在前端终端）
```

## 十、数据库完整表结构

| 表名 | 说明 |
|------|------|
| org | 组织表 |
| sys_user | 用户表 |
| role | 角色定义表 |
| role_page_permission | 角色-页面权限关联表 |
| user_page_permission | 用户-页面权限关联表（覆盖角色权限） |
| user_page_content | 用户页面内容表（历史，已被实例表替代） |
| user_page_instance | 用户页面实例表（新，支持多实例） |
| audit_log | 操作日志表 |
| flyway_schema_history | Flyway 迁移记录表 |
