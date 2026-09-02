# TOGAF 企业架构开发规范（精简版）

> 适用场景：本项目整体架构设计与交付物管理
> 核心思想：用 ADM（架构开发方法）规范化地推进架构工作

---

## 一、ADM 架构开发循环

### 1.1 循环流程图

```mermaid
flowchart TB
    A["预阶段<br>架构工作定义"] --> B["B 架构愿景"]
    B --> C["C 业务架构"]
    C --> D1["D1 数据架构"]
    C --> D2["D2 应用架构"]
    C --> D3["D3 技术架构"]
    D1 --> E["E 机会与解决方案"]
    D2 --> E
    D3 --> E
    E --> F["F 迁移规划"]
    F --> G["G 实施监督"]
    G --> H["H 架构变更管理"]
    H --> A
    H -.->|反馈调整| B
    style A fill:#f9f,stroke:#333
    style H fill:#f9f,stroke:#333
```

### 1.2 各阶段产出物对照

| 阶段 | 名称 | 本项目产出物 | 状态 |
|------|------|------------|------|
| 预阶段 | 架构工作定义 | 项目章程、架构治理规则 | 已有（合作意向书） |
| B | 架构愿景 | docs/vision.md | 待创建 |
| C | 业务架构 | docs/business-architecture.md | 待创建 |
| D1 | 信息系统-数据架构 | docs/data-architecture.md | 待创建 |
| D2 | 信息系统-应用架构 | docs/application-architecture.md | 待创建 |
| D3 | 技术架构 | docs/technology-architecture.md | 待创建 |
| E | 机会与解决方案 | docs/roadmap.md | 待创建 |
| F | 迁移规划 | docs/migration-plan.md | 待创建 |
| G | 实施监督 | CI/CD 流水线、监控 | 待创建 |
| H | 架构变更管理 | 变更日志、版本记录 | 待创建 |

---

## 二、架构交付物模板

### 2.1 架构愿景文档（Phase B）

**文件：docs/vision.md**

#### 1. 项目背景
- 业务驱动力
- 利益相关方清单

#### 2. 架构约束
- 技术约束（必须使用 PostgreSQL）
- 业务约束（3D前端由其他方负责）
- 非功能约束（兼职开发，周期3-5个月）

#### 3. 关键决策记录

| 决策 | 选项A | 选项B | 选择 | 理由 |
|------|-------|-------|------|------|
| 后端语言 | Java/Spring Boot | Node.js/Express | ? | ? |

#### 4. 验收标准
- 功能验收项
- 性能指标（API响应 < 200ms）
- 安全指标

---

### 2.2 业务架构文档（Phase C）

**文件：docs/business-architecture.md**

#### 1. 用户角色与权限矩阵

| 角色 | 客户管理 | 订单管理 | 合同管理 | 项目管理 | 系统管理 |
|------|---------|---------|---------|---------|---------|
| 销售 | CRUD | R | R | R | - |
| 项目经理 | R | CRUD | CRUD | CRUD | - |
| 财务 | R | R | CRUD | - | - |
| 管理员 | CRUD | CRUD | CRUD | CRUD | CRUD |

#### 2. 业务能力地图
- 客户能力域：客户录入、客户跟进、客户分层
- 交易能力域：订单创建、订单跟踪、合同管理
- 项目能力域：项目规划、进度管理、资源分配

---

### 2.3 数据架构文档（Phase D1）

**文件：docs/data-architecture.md**

#### 1. 核心实体关系图

```mermaid
flowchart TB
    subgraph C["CUSTOMER 客户"]
        C_ID["id PK"]
        C_NAME["name"]
        C_EMAIL["email"]
        C_PHONE["phone"]
        C_TYPE["type"]
        C_EXTRA["extra_params JSONB"]
        C_DEL["is_deleted"]
    end
    subgraph SO["SALES_ORDER 订单"]
        SO_ID["id PK"]
        SO_NO["order_no UNIQUE"]
        SO_CID["customer_id FK"]
        SO_PID["project_id FK"]
        SO_STATUS["status"]
        SO_AMOUNT["total_amount"]
    end
    subgraph CO["CONTRACT 合同"]
        CO_ID["id PK"]
        CO_NO["contract_no UNIQUE"]
        CO_OID["order_id FK"]
        CO_CID["customer_id FK"]
        CO_SIGN["sign_date"]
        CO_EXPIRE["expire_date"]
        CO_AMOUNT["amount"]
    end
    subgraph PI["PROJECT 项目"]
        PI_ID["id PK"]
        PI_CODE["project_code UNIQUE"]
        PI_CID["customer_id FK"]
        PI_NAME["name"]
        PI_STATUS["status"]
        PI_PROGRESS["progress_percent"]
    end
    subgraph OI["ORDER_ITEM 订单明细"]
        OI_ID["id PK"]
        OI_OID["order_id FK"]
        OI_PID["product_id FK"]
        OI_QTY["quantity"]
        OI_PRICE["unit_price"]
    end

    C -->|belongs to| SO
    C -->|owns| PI
    SO -->|has| CO
    SO -->|contains| OI

    style C fill:#e3f2fd,stroke:#1565c0
    style SO fill:#fff3e0,stroke:#e65100
    style CO fill:#e8f5e9,stroke:#2e7d32
    style PI fill:#fce4ec,stroke:#880e4f
    style OI fill:#f3e5f5,stroke:#4a148c
```

#### 2. 数据安全分级

| 级别 | 数据类型 | 加密要求 |
|------|---------|---------|
| P0 | 密码、密钥 | AES-256 |
| P1 | 手机号、身份证 | 脱敏存储 |
| P2 | 客户名称、邮箱 | 访问控制 |
| P3 | 订单金额、状态 | 常规安全 |

---

### 2.4 应用架构文档（Phase D2）

**文件：docs/application-architecture.md**

#### 1. 系统上下文图

```mermaid
flowchart LR
    FE["🌐 3D前端<br>Vue/React"] -->|REST API| API["⚙️ CRM后端<br>Spring Boot"]
    API --> DB[("🗄️ PostgreSQL<br>持久化存储")]
    API --> RD[("⚡ Redis<br>会话+缓存")]
    API --> MQ["📨 RabbitMQ<br>异步消息"]
    MQ --> PROC["🔧 异步处理器"]
    style FE fill:#e1f5fe,stroke:#01579b
    style API fill:#fff3e0,stroke:#e65100
    style DB fill:#e8f5e9,stroke:#2e7d32
    style RD fill:#fce4ec,stroke:#880e4f
    style MQ fill:#f3e5f5,stroke:#4a148c
```

#### 2. 模块划分

| 模块 | 职责 | 技术 | 接口 |
|------|------|------|------|
| auth | 认证授权 | JWT + BCrypt | /api/auth/* |
| customer | 客户管理 | CRUD + 搜索 | /api/customers/* |
| order | 订单管理 | 状态机 | /api/orders/* |
| contract | 合同管理 | 生命周期 | /api/contracts/* |
| project | 项目管理 | 进度跟踪 | /api/projects/* |
| report | 数据报表 | 聚合查询 | /api/reports/* |

#### 3. API 设计规范
- RESTful 风格
- 统一响应格式：`{code, message, data, timestamp}`
- 分页：`?page=1&size=20`
- 筛选：`?status=active&created_after=2026-01-01`

---

### 2.5 技术架构文档（Phase D3）

**文件：docs/technology-architecture.md**

#### 1. 技术栈选型

| 层级 | 技术 | 版本 | 选型理由 |
|------|------|------|---------|
| 运行时 | Java | 17 LTS | 企业级稳定性 |
| 框架 | Spring Boot | 3.x | 生态完善 |
| ORM | MyBatis-Plus | 3.5+ | 开发效率高 |
| 数据库 | PostgreSQL | 15+ | 本项目强制 |
| 缓存 | Redis | 7.x | 会话+热点数据 |
| 消息 | RabbitMQ | 3.x | 异步解耦 |
| 容器 | Docker | latest | 一键部署 |
| CI/CD | GitHub Actions | - | 自动化 |

#### 2. 部署架构图

```mermaid
flowchart TB
    subgraph CDN["CDN / 边缘节点"]
        STATIC["静态资源"]
    end
    subgraph GW["网关层"]
        NGINX["Nginx 反向代理<br>负载均衡 + SSL终止"]
    end
    subgraph APP["应用层（无状态，水平扩容）"]
        direction LR
        APP1["App-01<br>Java 17"]
        APP2["App-02<br>Java 17"]
        APP3["App-03<br>Java 17"]
    end
    subgraph CACHE["缓存层"]
        REDIS["Redis 哨兵模式<br>会话 + 热点数据"]
    end
    subgraph DB["数据层"]
        PG_MASTER["PostgreSQL 主库"]
        PG_SLAVE["PostgreSQL 从库<br>只读副本"]
    end
    subgraph MQ["消息层"]
        RABBIT["RabbitMQ<br>异步解耦"]
    end

    CDN --> GW
    GW --> APP
    APP --> DB
    APP --> CACHE
    APP --> MQ
    PG_MASTER -.->|复制| PG_SLAVE

    style NGINX fill:#fff3e0,stroke:#e65100
    style APP1 fill:#e3f2fd,stroke:#1565c0
    style APP2 fill:#e3f2fd,stroke:#1565c0
    style APP3 fill:#e3f2fd,stroke:#1565c0
    style PG_MASTER fill:#e8f5e9,stroke:#2e7d32
    style PG_SLAVE fill:#e8f5e9,stroke:#2e7d32
    style REDIS fill:#fce4ec,stroke:#880e4f
    style RABBIT fill:#f3e5f5,stroke:#4a148c
```

#### 3. 高可用设计
- 数据库主从复制 + 自动故障切换
- 应用无状态设计，支持水平扩容
- Redis Sentinel 高可用
- 定期备份策略（每日全量 + 每小时增量）

---

### 2.6 迁移规划文档（Phase F）

**文件：docs/migration-plan.md**

#### 里程碑时间线

```mermaid
flowchart LR
    M1["M1 PRD + 架构文档\n2周"] --> M2["M2 核心CRUD模块\n4周"]
    M2 --> M3["M3 报表+多语言\n2周"]
    M3 --> M4["M4 部署上线\n1周"]
    M4 --> MAINT["维护期 3个月"]

    style M1 fill:#ffcdd2,stroke:#c62828
    style M2 fill:#fff9c4,stroke:#f9a825
    style M3 fill:#c8e6c9,stroke:#2e7d32
    style M4 fill:#e3f2fd,stroke:#1565c0
    style MAINT fill:#f3e5f5,stroke:#4a148c
```

| 阶段 | 交付物 | 工期 | 付款节点 |
|------|--------|------|---------|
| M1 | PRD + 架构文档 | 2周 | 20% |
| M2 | 核心CRUD模块 | 4周 | 30% |
| M3 | 报表+多语言 | 2周 | 10% |
| M4 | 部署上线 | 1周 | 10% |
| 维护 | 3个月维护期 | 3个月 | 尾款10% |

#### 风险缓解

| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|---------|
| 需求变更频繁 | 中 | 高 | 阶段验收+变更流程 |
| 客户付款延迟 | 中 | 中 | 预付款30%保底 |
| 技术债务累积 | 低 | 中 | 每迭代代码审查 |

---

## 三、架构决策记录（ADR）

每个重大技术决策用 ADR 记录：

```markdown
# ADR-001: 选择 PostgreSQL 作为主数据库

状态：已接受
日期：2026-08-20
上下文：项目需要强一致性事务、复杂查询、JSONB 支持扩展字段
决策：选用 PostgreSQL 15+，放弃 MySQL
后果：
- 优点：原生 JSONB、CTE 递归查询、更好的并发控制
- 缺点：团队需学习曲线、运维成本略高
```

---

## 四、架构治理

### 4.1 变更流程

```mermaid
flowchart LR
    REQ["提出变更请求"] --> EVAL["评估影响范围"]
    EVAL --> ADR["ADR 记录决策"]
    ADR --> ARCH["架构师审批"]
    ARCH --> IMPLEMENT["编码实施"]
    IMPLEMENT --> REVIEW["代码审查"]
    REVIEW --> TEST["回归测试"]
    TEST --> DONE["✅ 上线"]
    ARCH -- 驳回 --> REQ
    TEST -- 失败 --> IMPLEMENT
```

### 4.2 架构审查要点

- [ ] 是否符合分层架构？
- [ ] 是否有循环依赖？
- [ ] 数据库设计是否符合第三范式？
- [ ] API 是否遵循 RESTful 规范？
- [ ] 安全设计是否完整？
- [ ] 性能设计是否合理？
- [ ] 是否有关键的单点故障？

---

*文档创建日期：2026-08-20*
*参考来源：TOGAF 9.2 框架（架构开发方法 ADM）*
