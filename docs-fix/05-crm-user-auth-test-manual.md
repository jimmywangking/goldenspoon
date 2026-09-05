# crm-user-auth 完整操作与测试手册

> 更新时间：2026-09-05
> 状态：**已完成且全量验收通过 ✅**
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

### 默认测试账号（密码均为 test123456，除 admin 外）
| 用户名 | 密码 | 角色 | org_id | 说明 |
|--------|------|------|--------|------|
| admin | admin123 | ADMIN | null | 系统管理员，全部权限 |
| testuser | test123456 | ADMIN | 3 | 测试公司更新下的管理员 |
| orgadmin | test123456 | ORG_ADMIN | 3 | 组织管理员 |
| orguser1 | test123456 | USER | 3 | 组织内普通用户 |
| user1 | test123456 | USER | 3 | 普通用户 |
| orgmember | test123456 | USER | 3 | 组织成员 |
| otherorg | test123456 | USER | 1 | 示例组织用户 |
| orgmember2 | test123456 | USER | 6 | 软删除测试组织用户 |
| testunique999 | test123456 | USER | 3 | 其他人 |

---

## 二、API 完整测试

### 2.0 获取 Token（所有接口都必须先获取）

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "Token: ${TOKEN:0:40}..."
```

### 2.1 认证 API（无需鉴权即可访问，但 me 需要 Token）

#### 登录
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | python3 -m json.tool
```
**预期：** code=200，返回 token、refreshToken、expiresIn、user 信息

#### 刷新 Token
```bash
REFRESH=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['refreshToken'])")

curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}" | python3 -m json.tool
```
**预期：** code=200，返回新的 accessToken

#### 获取当前用户信息
```bash
curl -s http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
**预期：** 返回当前登录用户的完整信息，含 role、orgId、permissions

#### 错误码
| 场景 | HTTP | code | message |
|------|------|------|---------|
| 密码错误 | 400 | 400 | 用户名或密码错误 |
| Token 无效/过期 | — | 401 | Token无效或已过期 |
| 未携带 Token | 403 | — | 未认证 |

---

### 2.2 组织管理 API（仅 ADMIN）

#### 查询组织列表（分页）
```bash
curl -s "http://localhost:8080/api/orgs?page=1&size=20&keyword=测试" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
参数：`page`（页码）、`size`（每页条数）、`keyword`（名称模糊搜索）

#### 查询单个组织
```bash
curl -s http://localhost:8080/api/orgs/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 创建组织
```bash
curl -s -X POST http://localhost:8080/api/orgs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"新组织","contactName":"张三","contactPhone":"13900139000"}' \
  | python3 -m json.tool
```

#### 更新组织
```bash
curl -s -X PUT http://localhost:8080/api/orgs/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"更新后的组织名","contactName":"李四","contactPhone":"13800138000"}' \
  | python3 -m json.tool
```

#### 删除组织（软删除）
```bash
curl -s -X DELETE http://localhost:8080/api/orgs/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
> 注：is_deleted 设为 true，原数据保留。若该组织下有用户，用户 org_id 置为 null。

---

### 2.3 用户管理 API（ADMIN 查看全部，ORG_ADMIN 只看自己组织）

#### 查询用户列表（分页）
```bash
curl -s "http://localhost:8080/api/users?current=1&size=10&keyword=admin&role=ADMIN&orgId=1" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
参数：`current`、`size`、`keyword`（用户名/姓名搜索）、`role`、`orgId`

> **权限规则**：ADMIN 查看全部；ORG_ADMIN 只看 orgId 匹配的用户；USER 返回 403

#### 查询单个用户
```bash
curl -s http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 创建用户
```bash
curl -s -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"pass123456","realName":"新用户","role":"USER","orgId":1,"email":"new@example.com"}' \
  | python3 -m json.tool
```

#### 更新用户
```bash
curl -s -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"realName":"更新姓名","email":"updated@example.com","phone":"13800001111","role":"USER","isActive":true}' \
  | python3 -m json.tool
```

#### 重置密码
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

### 2.4 角色管理 API（仅 ADMIN）

#### 查询角色列表
```bash
curl -s "http://localhost:8080/api/roles?page=1&size=20&keyword=ADMIN" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

#### 查询单个角色
```bash
curl -s http://localhost:8080/api/roles/1 \
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

#### 更新角色
```bash
curl -s -X PUT http://localhost:8080/api/roles/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"更新角色","code":"TEST_ROLE","description":"更新描述","isSystem":false}' \
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

### 2.5 页面权限 API（用户维度的页面访问权限）

#### 查询用户权限
```bash
curl -s http://localhost:8080/api/users/1/permissions \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
**预期：** 返回数组，每项含 pageCode、canView、canEdit

#### 设置用户权限
```bash
curl -s -X PUT http://localhost:8080/api/users/1/permissions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"permissions":[{"pageCode":"PAGE_1","canView":true,"canEdit":true},{"pageCode":"PAGE_2","canView":true,"canEdit":false}]}' \
  | python3 -m json.tool
```

---

### 2.6 页面内容 API（JSON 内容读写）

#### 获取当前用户的页面内容
```bash
curl -s http://localhost:8080/api/pages/PAGE_1 \
  -H "Authorization: Bearer $TOKEN"
```
**预期：** 返回 JSON 字符串或空字符串

#### 保存页面内容（创建新版本）
```bash
curl -s -X POST http://localhost:8080/api/pages/PAGE_1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"[{\"id\":\"m1\",\"name\":\"墙体\",\"position\":{\"x\":0,\"y\":0,\"z\":0},\"rotation\":{\"x\":0,\"y\":0,\"z\":0},\"scale\":{\"x\":4,\"y\":3,\"z\":0.2},\"color\":\"#E8E8E8\"}]","versionName":"v1"}'
```
**预期：** 返回 `{"code":200,"message":"success"}`，每次保存生成新版本号

#### 查看版本历史
```bash
curl -s http://localhost:8080/api/pages/PAGE_1/versions \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
**预期：** 返回按 version DESC 排序的版本列表，包含 id/version/versionName/createdAt/content

#### 恢复到指定版本
```bash
curl -s -X POST http://localhost:8080/api/pages/PAGE_1/versions/2/restore \
  -H "Authorization: Bearer $TOKEN"
```
**预期：** 当前最新版本的 content 被替换为版本2的内容，versionName 标记为"恢复到版本 N"

#### 管理员查看某页面所有用户的内容
```bash
curl -s http://localhost:8080/api/pages/PAGE_1/all \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
**预期：** ADMIN 查看所有用户的所有版本，ORG_ADMIN 只看本组织用户的内容

---

### 2.7 页面实例 API（每个用户多个同类型实例）

#### 查询我的页面实例（分页）
```bash
curl -s "http://localhost:8080/api/instances?pageCode=PAGE_1&current=1&size=10" \
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
> **数量限制**：USER 每个 pageCode 最多 5 个，ORG_ADMIN 最多 20 个，ADMIN 无限制
> 超出返回：`{"code":400,"message":"超出页面实例数量限制（最多5个）","data":null}`

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
curl -s "http://localhost:8080/api/instances/admin/all?pageCode=PAGE_1&current=1&size=20" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

---

## 三、Swagger UI API 测试界面

访问 http://localhost:8080/swagger-ui

### 使用方法
1. 点击右上角 **Authorize** 按钮
2. 在 Value 输入框中粘贴完整 JWT token（不需要加 `Bearer ` 前缀，系统自动处理）
3. 点击 Authorize → Close
4. 展开任意接口，点击 **Try it out** → **Execute** 即可测试

### 认证方式
- 除 login/refresh 外所有接口都需要 Header `Authorization: Bearer <token>`
- Swagger UI 会自动将 token 添加到请求头

---

## 四、角色权限体系

### 角色说明

| 角色 | 说明 | 可见范围 | 特殊权限 |
|------|------|---------|---------|
| ADMIN | 系统管理员 | 全部组织、全部用户、所有页面 | 角色管理、查看他人实例 |
| ORG_ADMIN | 组织管理员 | 自己组织的用户、所有业务页面 | 查看同组织实例 |
| USER | 普通用户 | 按 page_permission 表授权页面 | 仅查看自己的实例 |

### 权限控制流程
```
登录 → JWT 携带 user 信息（含 role、orgId、orgName）
     → 路由守卫检查 meta.requiredPermission / requiresAdmin
     → 有权限 → 进入页面（可看到编辑按钮 if canEdit）
     → 无权限 → 跳转 /403 页面
```

### 页面权限来源（优先级从高到低）
1. `user_page_permission` 表（用户级别覆盖）
2. `role_page_permission` 表（角色级别默认）
3. ADMIN 角色默认拥有所有页面

---

## 五、前端页面测试

访问 http://localhost:5173

### 5.1 登录页（/login）
1. 输入 admin / admin123，点击登录
2. **预期：** 登录成功，跳转到首页 `/`
3. 输入错误密码 → 提示错误信息
4. 登录后访问 http://localhost:5173/login → 自动跳转到 `/`

### 5.2 首页（/）
- 显示欢迎信息
- 左侧菜单展示可访问的页面

### 5.3 组织管理（/orgs）
- 仅 ADMIN 可访问
- 支持搜索、新增、编辑、删除组织
- ORG_ADMIN / USER 访问 → 跳转 403

### 5.4 用户管理（/users）
- ADMIN 查看全部用户；ORG_ADMIN 只看自己组织
- 支持搜索（用户名/姓名）、按角色/组织筛选
- 新增用户时选择角色（USER/ORG_ADMIN/ADMIN）和所属组织
- 支持重置密码、软删除用户

### 5.5 角色管理（/roles）
- 仅 ADMIN 可访问
- 支持角色的 CRUD
- 可配置每个角色的页面权限（PAGE_1/PAGE_2）
- 系统角色（isSystem=true）不可删除

### 5.6 页面 1（/page1）— 3D 模块化住房编辑器
- Three.js 3D 场景，支持旋转/缩放/平移查看
- 左侧模块库：墙体、地板、屋顶、房间、车库、扩展模块
- 右侧属性面板：名称、颜色、位置、旋转、缩放编辑
- **保存设计**：每次保存创建新版本，版本号自动递增，versionName 记录时间戳
- **版本历史**：点击"版本历史"按钮，查看自己所有历史版本，支持一键恢复到任意版本
- **导出/导入 JSON**：导出当前设计为 JSON 文件，或从文件导入恢复
- **查看所有设计**（ADMIN/ORG_ADMIN）：弹出表格显示所有设计（含用户名、组织、版本号），可加载到编辑器或预览
- USER 只能编辑/查看自己的设计，ORG_ADMIN 可查看本组织所有用户的设计
- 无权限 → 路由拦截跳转 403

### 5.7 页面 2（/page2）
- 同 PAGE_1，独立的内容存储

### 5.8 页面实例管理（/instances）
- 下拉选择页面类型（PAGE_1 / PAGE_2）
- 显示"已有 X 个实例（最多 Y 个）"
- 支持新建/编辑/删除实例
- 每个实例独立保存 JSON 内容

### 5.9 403 页面（/403）
- 无权限时显示"您没有权限访问此页面"
- 提供"返回首页"按钮

### 权限验证完整流程
```
1. admin 登录，创建用户 testuser（role=USER, orgId=3）
2. 给用户 testuser 只分配 PAGE_1 权限（canView=true, canEdit=false）
3. 退出，用 testuser/test123456 登录
4. 验证：能进入 PAGE_1（只读），不能进入 PAGE_2（跳转 403）
5. admin 给 testuser 也分配 PAGE_2 编辑权限
6. 再次用 testuser 登录，验证可以进入 PAGE_2 并可编辑
7. orgadmin 登录，验证只能看到 orgId=3 的用户
8. user1 登录，尝试访问用户管理页面 → 403
```

---

## 六、数据库操作

### 6.1 查看所有表
```bash
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\dt"
```

### 6.2 常用 SQL
```bash
docker exec -it pg-local psql -U postgres -d crm_user_auth

-- 组织
SELECT id, name, contact_name, contact_phone, is_active, is_deleted FROM org;

-- 用户
SELECT id, username, real_name, role, org_id, is_active, is_deleted FROM sys_user;

-- 角色
SELECT id, name, code, description, is_system, is_deleted FROM role;

-- 角色页面权限
SELECT r.name as role_name, rpp.page_code FROM role r
JOIN role_page_permission rpp ON r.id = rpp.role_id WHERE r.is_deleted = false;

-- 用户页面权限
SELECT u.username, p.page_code, p.can_view, p.can_edit FROM user_page_permission p
JOIN sys_user u ON p.user_id = u.id WHERE p.is_deleted = false;

-- 页面实例
SELECT id, user_id, page_code, title, LEFT(content, 80) as content_preview, is_deleted
FROM user_page_instance ORDER BY created_at DESC;

-- 操作日志
SELECT id, operator_id, action, target_type, created_at FROM audit_log
ORDER BY created_at DESC LIMIT 20;

-- 统计各用户实例数量
SELECT u.username, i.page_code, COUNT(*) as instance_count
FROM user_page_instance i JOIN sys_user u ON i.user_id = u.id
WHERE i.is_deleted = false GROUP BY u.username, i.page_code;

\q
```

### 6.3 查看表结构
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

### 6.4 数据库表说明

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| org | 组织表 | id, name, contact_name, contact_phone, is_active, is_deleted |
| sys_user | 用户表 | id, username, password(BCrypt), real_name, role, org_id, is_active, is_deleted |
| role | 角色定义表 | id, name, code, description, is_system, is_deleted |
| role_page_permission | 角色-页面权限关联 | role_id, page_code |
| user_page_permission | 用户-页面权限关联（覆盖角色） | user_id, page_code, can_view, can_edit |
| user_page_content | 用户页面内容（历史表） | user_id, page_code, content |
| user_page_instance | 用户页面实例（新表） | user_id, page_code, title, content, sort_order |
| audit_log | 操作日志 | operator_id, action, target_type, target_id |

---

## 七、常见问题排查

### 登录返回 400 "用户名或密码错误"
```bash
docker exec pg-local psql -U postgres -d crm_user_auth \
  -c "SELECT password FROM sys_user WHERE username='admin' AND is_deleted=false;"
# 正确值：$2a$12$7btG3/9wRTMly7X2QkzdmuoqUorGIvqg/HKEFJ3OfdrqeWC/sQyH2
```

### Swagger UI 没有 Authorize 按钮
- 强制刷新浏览器：Ctrl+Shift+R（Mac: Cmd+Shift+R）
- 清除浏览器缓存后重新访问 http://localhost:8080/swagger-ui

### 端口被占用
```bash
lsof -i:8080 && kill <PID>
lsof -i:5173 && kill <PID>
```

### 数据库连接失败
```bash
docker ps | grep pg-local
docker restart pg-local
```

### 页面实例数量限制
- 创建第 6 个实例（USER）时返回 400
- 查看当前数量：
  ```bash
  docker exec pg-local psql -U postgres -d crm_user_auth \
    -c "SELECT user_id, page_code, COUNT(*) FROM user_page_instance WHERE is_deleted=false GROUP BY user_id, page_code;"
  ```

---

## 八、服务启动/停止

### 后端
```bash
# 启动
java -jar /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/backend/target/crm-user-auth-1.0.0-SNAPSHOT.jar > /tmp/backend.log 2>&1 &

# 停止
pkill -f crm-user-auth

# 查看日志
tail -f /tmp/backend.log
```

### 前端
```bash
# 启动
cd /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/frontend && npm run dev

# 构建
npm run build

# 停止
Ctrl+C
```

### 完整编译部署
```bash
cd /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/backend
mvn clean package -DskipTests -q
java -jar target/crm-user-auth-1.0.0-SNAPSHOT.jar > /tmp/backend.log 2>&1 &
```

---

## 九、API 汇总

| 模块 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 认证 | POST | /api/auth/login | 公开 | 登录获取 Token |
| 认证 | POST | /api/auth/refresh | 公开 | 刷新 Token |
| 认证 | GET | /api/auth/me | 已登录 | 获取当前用户信息 |
| 组织 | GET | /api/orgs | ADMIN | 分页查询组织 |
| 组织 | GET | /api/orgs/{id} | ADMIN | 查询组织详情 |
| 组织 | POST | /api/orgs | ADMIN | 创建组织 |
| 组织 | PUT | /api/orgs/{id} | ADMIN | 更新组织 |
| 组织 | DELETE | /api/orgs/{id} | ADMIN | 删除组织 |
| 用户 | GET | /api/users | ADMIN/ORG_ADMIN | 分页查询用户 |
| 用户 | GET | /api/users/{id} | ADMIN | 查询用户详情 |
| 用户 | POST | /api/users | ADMIN | 创建用户 |
| 用户 | PUT | /api/users/{id} | ADMIN | 更新用户 |
| 用户 | DELETE | /api/users/{id} | ADMIN | 删除用户 |
| 用户 | POST | /api/users/{id}/reset-password | ADMIN | 重置密码 |
| 角色 | GET | /api/roles | ADMIN | 分页查询角色 |
| 角色 | GET | /api/roles/{id} | ADMIN | 查询角色详情 |
| 角色 | POST | /api/roles | ADMIN | 创建角色 |
| 角色 | PUT | /api/roles/{id} | ADMIN | 更新角色 |
| 角色 | DELETE | /api/roles/{id} | ADMIN | 删除角色 |
| 角色 | GET | /api/roles/{id}/permissions | ADMIN | 获取角色页面权限 |
| 角色 | PUT | /api/roles/{id}/permissions | ADMIN | 设置角色页面权限 |
| 权限 | GET | /api/users/{userId}/permissions | ADMIN | 查询用户页面权限 |
| 权限 | PUT | /api/users/{userId}/permissions | ADMIN | 设置用户页面权限 |
| 内容 | GET | /api/pages/{pageCode} | 已登录 | 获取当前用户最新页面内容 |
| 内容 | POST | /api/pages/{pageCode} | 可编辑 | 保存页面内容（创建新版本） |
| 内容 | GET | /api/pages/{pageCode}/versions | 已登录 | 查询版本历史 |
| 内容 | POST | /api/pages/{pageCode}/versions/{id}/restore | 已登录 | 恢复到指定版本 |
| 内容 | GET | /api/pages/{pageCode}/all | ADMIN/ORG_ADMIN | 查看所有用户页面内容 |
| 实例 | GET | /api/instances | 已登录 | 分页查询我的实例 |
| 实例 | GET | /api/instances/{id} | 已登录 | 获取实例详情 |
| 实例 | POST | /api/instances | 已登录 | 创建实例 |
| 实例 | PUT | /api/instances/{id} | 已登录 | 更新实例 |
| 实例 | DELETE | /api/instances/{id} | 已登录 | 删除实例 |
| 实例 | GET | /api/instances/admin/all | ADMIN | 管理员查看所有实例 |

**共计 35 个 API 接口**
