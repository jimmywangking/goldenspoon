# crm-user-auth 操作手册

> 更新时间：2026-08-25
> 状态：**已完成且全量验收通过（含组织管理员角色、权限过滤、用户下拉框）✅**
> 后端：http://localhost:8080 | 前端：http://localhost:5173
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
| ADMIN | admin | admin123 | 所有页面（PAGE_1 + PAGE_2 查看+编辑）、组织/用户/角色管理 |
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
# 执行登录
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
# 查看数据库中 admin 密码哈希是否正确
docker exec pg-local psql -U postgres -d crm_user_auth \
  -c "SELECT password FROM sys_user WHERE username='admin' AND is_deleted=false;"
```
正确值应为：`$2a$12$7btG3/9wRTMly7X2QkzdmuoqUorGIvqg/HKEFJ3OfdrqeWC/sQyH2`

**如果密码不对，执行修复：**
```bash
python3 -c "
import bcrypt
pw = bcrypt.hashpw(b'admin123', bcrypt.gensalt(rounds=12))
print(pw.decode())
"
# 然后把输出的哈希更新到数据库
```

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
参数：`page`、`size`、`keyword`（用户名/姓名模糊搜索）、`role`（ADMIN/USER/ORG_ADMIN）、`orgId`（按组织筛选）

> 响应中包含 `orgName` 字段（来自 org 表 LEFT JOIN），前端表格直接显示组织名称而非 ID。
> **权限规则**：ADMIN 查看全部；ORG_ADMIN 只看自己组织；普通用户返回 403。

#### 查询单个用户详情
```bash
curl -s http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```
> 响应包含 `orgName` 字段。

#### 创建用户
```bash
curl -s -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"pass123456","realName":"新用户","role":"USER","orgId":1}' \
  | python3 -m json.tool
```
> 注意：密码会经 BCrypt（强度12）加密存储，响应中不会返回明文密码

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

### 2.5 权限管理 API

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
      {"pageCode": "PAGE_1", "canView": true, "canEdit": false},
      {"pageCode": "PAGE_2", "canView": true, "canEdit": true}
    ]
  }' | python3 -m json.tool
```

---

## 三、页面权限规则

### 角色说明

| 角色 | 说明 | 可见范围 |
|------|------|---------|
| ADMIN | 系统管理员 | 全部组织、全部用户、所有页面 |
| ORG_ADMIN | 组织管理员 | 自己组织的用户、所有页面 |
| USER | 普通用户 | 仅拥有 PAGE_1/PAGE_2 权限的页面 |

### 权限控制流程

```
登录 → JWT 携带 user 信息（含 role、orgId、orgName）
     → 路由守卫检查 meta.requiredPermission / requiresAdmin
     → 有权限 → 进入页面（可看到编辑按钮 if canEdit）
     → 无权限 → 跳转 /403 页面
```

### 页面 1 / 页面 2 示例功能

- 管理员（ADMIN/ORG_ADMIN）：始终可见"可编辑"标签，可修改内容并保存
- 普通用户（canView=true, canEdit=false）：仅查看内容，无编辑按钮
- 普通用户（无 PAGE_1/PAGE_2 权限）：路由守卫拦截，跳转到 403 页
- 403 页面：显示"您没有权限访问此页面"，有"返回首页"按钮

### 创建用户时的角色选择

前端新建/编辑用户时，角色下拉选项：
- 普通用户（USER）
- 组织管理员（ORG_ADMIN）
- 系统管理员（ADMIN）

---

## 四、数据库操作

### 常用 SQL 查询
```bash
# 进入数据库
docker exec -it pg-local psql -U postgres -d crm_user_auth

-- 查看所有组织
SELECT id, name, is_active, is_deleted FROM org;

-- 查看所有用户
SELECT id, username, role, is_active, is_deleted FROM sys_user;

-- 查看用户权限
SELECT u.username, p.page_code, p.can_view, p.can_edit
FROM user_page_permission p
JOIN sys_user u ON p.user_id = u.id
WHERE p.is_deleted = false;

-- 查看操作日志
SELECT id, operator_id, action, target_type, created_at
FROM audit_log ORDER BY created_at DESC LIMIT 20;

\q
```

### 查看表结构
```bash
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d org"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d sys_user"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d user_page_permission"
docker exec -it pg-local psql -U postgres -d crm_user_auth -c "\d audit_log"
```

---

## 五、前端页面测试

访问 http://localhost:5173

### 测试流程
1. 打开浏览器访问 http://localhost:5173
2. 登录页输入 admin / admin123
3. 登录成功后可访问：组织管理、用户管理、页面1、页面2
4. 退出登录后可重新用 testuser 登录，验证权限控制

### 页面权限验证
1. 用 admin 登录，创建一个新用户 testuser，密码 test123456
2. 在用户管理页：
   - 点击"新增用户"，**所属组织**选择下拉框中的组织名称（不再需要手动输入 ID）
   - **角色**选择下拉：普通用户/组织管理员/系统管理员
   - 保存后，在用户列表看到 `orgName` 列显示组织名称而非数字 ID
3. 给用户分配权限：通过 `/api/users/{id}/permissions` 接口或前端权限管理
4. 退出，用 testuser/test123456 登录
5. 验证：能进入 PAGE_1，不能进入 PAGE_2（路由守卫拦截，显示 403 页面）
6. 用 admin 给 testuser 也分配 PAGE_2 编辑权限
7. 再次用 testuser 登录，验证可以进入 PAGE_2 并编辑
8. 用 orgadmin 登录，验证只能看到自己组织的用户（如"测试公司更新"下的用户）
9. 用普通用户 user1 登录，尝试访问用户管理页面，验证被拦截（403）

---

## 六、常见问题排查

### 登录返回 400 "用户名或密码错误"
```bash
# 检查数据库中 admin 密码哈希是否正确
docker exec pg-local psql -U postgres -d crm_user_auth \
  -c "SELECT password FROM sys_user WHERE username='admin' AND is_deleted=false;"
# 正确值应为 $2a$12$7btG3/9wRTMly7X2QkzdmuoqUorGIvqg/HKEFJ3OfdrqeWC/sQyH2
```

### 端口被占用
```bash
# 查看占用进程
lsof -i:8080
# 杀掉进程
kill <PID>
```

### 数据库连接失败
```bash
# 检查 Docker 容器状态
docker ps | grep pg-local
# 重启容器
docker restart pg-local
```

### 测试用户不存在
```bash
# 创建测试用户
curl -s -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123456","realName":"测试用户","role":"USER","orgId":1}'
```

---

## 七、后端启动/停止

```bash
# 启动
java -jar /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/backend/target/crm-user-auth-1.0.0-SNAPSHOT.jar > /tmp/backend.log 2>&1 &

# 停止
kill $(lsof -ti:8080)

# 查看日志
tail -f /tmp/backend.log

# 重新编译
mvn clean package -DskipTests -f /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/backend/pom.xml
```

## 八、前端启动/停止

```bash
# 启动
cd /Users/helloworld/Downloads/AI/20260813D/crm-user-auth/frontend && npm run dev

# 构建
npm run build

# 停止
Ctrl+C（在前端终端）
```
