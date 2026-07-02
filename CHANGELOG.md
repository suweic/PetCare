# Changelog

本文件记录 PetCare 项目的所有重要变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

---

## [1.0.0] — 2026-07-02

### Added — 新增

- **C端（用户端）**
  - 手机号+密码 / 短信验证码双模式登录
  - 宠物档案管理（创建、编辑、查看、删除）
  - 医生浏览（科室筛选、搜索、分页、详情）
  - AI 预问诊模块（症状描述 → LLM 推荐科室和医生）
  - WebSocket 实时图文问诊聊天室
  - 问诊记录查看（全部/进行中/已完成）
  - 处方查看（诊断、药品、用法用量、医嘱）
  - 医生评价（星级评分 + 文字评价）

- **管理后台**
  - 数据看板（统计卡片 + ECharts 图表）
  - 医生资质审核（通过/拒绝 + 审核意见）
  - 医生管理（搜索、列表、启用/禁用）
  - 用户管理（搜索、列表、禁用/启用）
  - 问诊记录管理（搜索、列表、详情弹窗）
  - 三级角色权限（超级管理员/运营管理员/审核员）

- **安全**
  - JWT 双通道认证（用户端 + 管理端）
  - AES-256-GCM 敏感信息加密
  - 限流（全局限流 + 登录限流）
  - 暴力破解防护（5次/15分钟锁定）
  - 文件上传安全校验（魔术字节 + 类型白名单 + 防路径遍历）

- **基础设施**
  - Docker 多阶段构建 + docker-compose 一键部署
  - MySQL 8.0 + Redis 7
  - Nginx 反向代理 + WebSocket 支持 + 安全响应头
  - 25 张数据库表（外键约束、索引、逻辑删除）
  - Knife4j / OpenAPI 3 交互式 API 文档
  - GitHub Actions CI/CD 流水线

- **工程化**
  - ESLint + Prettier 代码规范（前端）
  - Vitest 前端测试框架
  - 环境变量分离（.env.development / .env.production）
  - Flyway 数据库迁移
  - LLM 调用超时/重试/熔断保护

- **社区**
  - Apache 2.0 开源许可证
  - README.md（完整项目文档）
  - CONTRIBUTING.md（贡献指南）
  - CODE_OF_CONDUCT.md（行为准则）
  - GitHub Issue/PR 模板
  - DEPLOYMENT_GUIDE.md（生产部署指南）

---

## [未发布]

### Planned — 计划中

- 微信/支付宝支付集成
- 阿里云/腾讯云短信 SDK 接入
- OSS 云存储集成（阿里云 OSS / 腾讯云 COS）
- JWT HttpOnly Secure Cookie + Refresh Token 模式
- 端到端测试（Playwright/Cypress）
- 国际化（i18n）
- 消息推送
