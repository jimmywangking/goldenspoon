# CRM 用户认证 API 文档

> Base URL: `http://localhost:8080`
> Auth: Bearer Token（从登录接口获取）

---

## 1. 认证接口 `/api/auth`

### 1.1 登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "role": "ADMIN",
      "orgId": null,
      "orgName": null,
      "permissions": [...],
      "admin": true
    }
  }
}
```

### 1.2 刷新 Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJ..."
}
```

### 1.3 获取当前用户信息
```http
GET /api/auth/me
Authorization: Bearer <token>
```

---

## 2. 用户管理 `/api/users`

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/users | ADMIN | 分页查询用户列表 |
| GET | /api/users/{id} | ADMIN | 获取用户详情 |
| POST | /api/users | ADMIN | 创建用户 |
| PUT | /api/users/{id} | ADMIN | 更新用户 |
| DELETE | /api/users/{id} | ADMIN | 删除用户 |
| POST | /api/users/{id}/reset-password | ADMIN | 重置密码 |

### 查询参数
- `current` (default: 1) - 页码
- `size` (default: 10) - 每页数量
- `keyword` - 搜索关键词
- `role` - 角色过滤
- `orgId` - 组织ID过滤

### 请求示例（创建用户）
```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "newuser",
  "password": "test123",
  "realName": "新用户",
  "role": "USER",
  "orgId": 3,
  "email": "user@example.com"
}
```

---

## 3. 组织管理 `/api/orgs`

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/orgs | ADMIN | 分页查询组织列表 |
| GET | /api/orgs/{id} | ADMIN | 获取组织详情 |
| POST | /api/orgs | ADMIN | 创建组织 |
| PUT | /api/orgs/{id} | ADMIN | 更新组织 |
| DELETE | /api/orgs/{id} | ADMIN | 删除组织 |

---

## 4. 角色管理 `/api/roles`

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/roles | ADMIN | 分页查询角色列表 |
| GET | /api/roles/{id} | ADMIN | 获取角色详情 |
| POST | /api/roles | ADMIN | 创建角色 |
| PUT | /api/roles/{id} | ADMIN | 更新角色 |
| DELETE | /api/roles/{id} | ADMIN | 删除角色 |
| GET | /api/roles/{id}/permissions | ADMIN | 获取角色页面权限 |
| PUT | /api/roles/{id}/permissions | ADMIN | 设置角色页面权限 |

---

## 5. 页面权限 `/api/users/{userId}/permissions`

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/users/{userId}/permissions | ADMIN | 获取用户页面权限 |
| PUT | /api/users/{userId}/permissions | ADMIN | 设置用户页面权限 |

### 请求示例
```http
PUT /api/users/1/permissions
Authorization: Bearer <token>
Content-Type: application/json

[
  {"pageCode": "PAGE_1", "canView": true, "canEdit": true},
  {"pageCode": "PAGE_2", "canView": true, "canEdit": false}
]
```

---

## 6. 页面内容 `/api/pages/{pageCode}`

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/pages/{pageCode} | 已登录 | 获取当前用户的页面内容 |
| PUT | /api/pages/{pageCode} | 可编辑 | 保存页面内容 |
| GET | /api/pages/{pageCode}/all | ADMIN/ORG_ADMIN | 获取所有用户的页面内容 |

### 请求示例
```http
PUT /api/pages/PAGE_1
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "{\"widgets\":[{\"type\":\"chart\",\"x\":0,\"y\":0}]}"
}
```

---

## 7. 页面实例 `/api/instances`

| Method | Path | 权限 | 说明 |
|--------|------|------|------|
| GET | /api/instances | 已登录 | 分页查询我的实例 |
| GET | /api/instances/{id} | 已登录 | 获取实例详情 |
| POST | /api/instances | 已登录 | 创建实例 |
| PUT | /api/instances/{id} | 已登录 | 更新实例 |
| DELETE | /api/instances/{id} | 已登录 | 删除实例 |
| GET | /api/instances/admin/all | ADMIN | 管理员查看所有实例 |

### 查询参数（GET /api/instances）
- `pageCode` - 页面类型过滤
- `current` (default: 1) - 页码
- `size` (default: 10) - 每页数量

### 请求示例（创建实例）
```http
POST /api/instances
Authorization: Bearer <token>
Content-Type: application/json

{
  "pageCode": "PAGE_1",
  "title": "我的设计",
  "content": "{\"layout\":\"custom\"}"
}
```

### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 38,
    "userId": 8,
    "pageCode": "PAGE_2",
    "title": "测试页面",
    "content": "hello world",
    "sortOrder": null,
    "createdBy": 8,
    "createdAt": "2026-09-04T13:33:20.887988+08:00",
    "updatedAt": "2026-09-04T13:33:20.89052+08:00"
  }
}
```

### 数量限制
- USER: 每个 pageCode 最多 5 个实例
- ORG_ADMIN: 每个 pageCode 最多 20 个实例
- ADMIN: 无限制

超出限制返回：
```json
{
  "code": 400,
  "message": "超出页面实例数量限制（最多5个）",
  "data": null
}
```

---

## 角色说明

| 角色 | 可见范围 | 特殊权限 |
|------|---------|---------|
| ADMIN | 全部组织、全部用户、所有页面 | 角色管理、查看他人实例 |
| ORG_ADMIN | 自己组织的用户、所有业务页面 | 查看同组织实例 |
| USER | 按 permission 表授权页面 | 仅查看自己的实例 |

---

## 错误响应格式

```json
{
  "code": 400,
  "message": "错误描述",
  "data": null,
  "timestamp": 1788500000000
}
```

| Code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token 无效） |
| 403 | 无权限访问 |
| 500 | 服务器内部错误 |
