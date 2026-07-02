# 贡献指南

感谢你对 PetCare 项目的关注！本文档将帮助你了解如何参与贡献。

## 行为准则

本项目遵循 [贡献者公约](CODE_OF_CONDUCT.md)。参与本项目即表示你同意遵守其条款。

## 如何贡献

### 报告 Bug

1. 在提交 Bug 报告前，请先搜索 [Issues](../../issues) 确认是否已有相同报告
2. 使用 Bug Report 模板提交 Issue
3. 尽可能提供详细信息：环境信息、复现步骤、期望行为、实际行为、截图/日志

### 建议新功能

1. 先搜索 [Issues](../../issues) 确认是否已有类似建议
2. 使用 Feature Request 模板提交 Issue
3. 描述你的使用场景和期望的解决方案

### 提交代码

1. **Fork 仓库** 到你自己的 GitHub 账号
2. **创建分支**：`git checkout -b feat/your-feature-name`
3. **编写代码**：遵循项目的代码规范和测试要求
4. **添加测试**：确保新功能有对应的测试覆盖
5. **确保通过**：`mvn test`（后端）、`npm run test`（前端）
6. **提交代码**：使用语义化提交信息（见下文）
7. **发起 Pull Request**：描述你的改动内容和原因

### 分支命名

| 类型 | 格式 | 示例 |
|------|------|------|
| 新功能 | `feat/描述` | `feat/payment-integration` |
| 修复 | `fix/描述` | `fix/login-timeout` |
| 重构 | `refactor/描述` | `refactor/extract-common-component` |
| 文档 | `docs/描述` | `docs/api-guide` |

### 提交信息规范

本项目使用 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/)：

```
<type>(<scope>): <subject>

<body>
```

类型：
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档变更
- `style`: 格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具变更

### 代码规范

- **后端**: 遵循 Java 命名规范，使用 Lombok 简化代码，Service 层需有对应单元测试
- **前端**: 遵循 Vue 3 Composition API + `<script setup>` 风格，使用 TypeScript
- **格式化**: 前端代码提交前运行 `npm run format && npm run lint`

## 开发环境

参见 [README.md](README.md) 的"快速开始"部分。

## 项目结构

```
petcare/
├── petcare-server/       # Spring Boot 后端 (Java 17)
│   ├── petcare-common/   # 公共模块
│   ├── petcare-security/ # 安全认证模块
│   └── petcare-system/   # 核心业务模块
├── petcare-web/          # Vue 3 用户端
├── petcare-admin/        # Vue 3 管理后台
└── docker-compose.yml    # Docker 编排
```

## 测试

- 后端测试位于 `petcare-server/*/src/test/`
- 前端测试位于各项目的 `src/__tests__/`
- 提交 PR 时 CI 会自动运行测试

## 问题反馈

如有任何问题，请通过 [Issues](../../issues) 联系我们。
