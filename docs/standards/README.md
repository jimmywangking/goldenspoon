# 项目规范索引

> 本项目全套开发规范文档集合
> 创建日期：2026-08-20

---

## 文档列表

| 编号 | 文档名 | 用途 | 优先级 |
|------|--------|------|--------|
| 01 | [Java 开发规范](./01-java-development-standards.md) | 编码风格、命名、异常、日志、SQL | ⭐⭐⭐ |
| 02 | [TDD 测试规范](./02-tdd-development-standards.md) | 测试驱动开发流程、覆盖率、工具链 | ⭐⭐⭐ |
| 03 | [PostgreSQL 设计规范](./03-postgresql-design-standards.md) | 建表、索引、分区、性能优化 | ⭐⭐⭐ |
| 04 | [TOGAF 架构规范](./04-togaf-architecture-standards.md) | 架构开发流程、交付物模板、ADR | ⭐⭐ |

---

## 规范使用顺序

```
项目启动
    │
    ▼
[TOGAF Phase B] 架构愿景 ──────────────────────────┐
    │                                               │
    ▼                                               │
[TOGAF Phase C] 业务架构                            │
    │                                               │
    ▼                                               │
[PostgreSQL规范] 数据库设计 ────────────────────────┤
    │                                               │
    ▼                                               │
[Java规范] 代码开发（同时遵守 TDD）                   │
    │                                               │
    ▼                                               │
[TDD规范] 测试覆盖 + 代码质量                        │
    │                                               │
    ▼                                               │
[TOGAF Phase F] 迁移规划与部署                       │
    │                                               │
    ▼                                               │
[TOGAF Phase G/H] 实施监督 + 架构变更管理            │
```

---

## 快速检查清单

### 提交代码前
- [ ] 单元测试覆盖率达标
- [ ] 代码符合 Java 命名和格式规范
- [ ] 没有 `System.out.println`
- [ ] 没有硬编码敏感信息
- [ ] SQL 不使用 `SELECT *`
- [ ] 新增表有 `created_at/updated_at/is_deleted`

### 设计数据库前
- [ ] 表名用单数 snake_case
- [ ] 主键统一 `BIGSERIAL`
- [ ] 金额用 `NUMERIC` 不用 `FLOAT`
- [ ] 时间用 `TIMESTAMPTZ`
- [ ] 高频查询字段已建索引
- [ ] 外键字段有索引

### 架构变更时
- [ ] 记录 ADR（架构决策记录）
- [ ] 评估对现有模块的影响
- [ ] 通知相关开发者

---

*维护者：乙方架构师*
