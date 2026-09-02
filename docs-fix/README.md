# 项目规范文档

> CRM + 3D 模块化住房管理系统 · 技术规范

---

## 文档清单

| 文件 | 内容 | 阶段 |
|------|------|------|
| [01-java-development-standards.md](./01-java-development-standards.md) | 阿里巴巴 Java 开发手册（核心要点） | 开发前 |
| [02-tdd-development-standards.md](./02-tdd-development-standards.md) | TDD 测试驱动开发规范 | 开发中 |
| [03-postgresql-design-standards.md](./03-postgresql-design-standards.md) | PostgreSQL 数据库设计规范 | 开发前 |
| [04-togaf-architecture-standards.md](./04-togaf-architecture-standards.md) | TOGAF 架构开发规范（精简版） | 架构阶段 |

---

## 使用顺序

```
Phase A（需求）  →  04 TOGAF     ← 确定架构方向、里程碑、ADR
Phase B（设计）  →  03 PostgreSQL ← 建表、写 SQL、跑 DDL
Phase C（开发）  →  01 Java      ← 编码、分层、安全
                 →  02 TDD       ← 先写测试，再写实现
```

---

## 图表说明

所有文档中的流程图均使用 **Mermaid** 语法，VS Code 预览时自动渲染为矢量图，无需安装额外插件。
