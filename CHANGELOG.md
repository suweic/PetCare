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

### Changed — 变更

- **修复** application.yml 重复 `spring:` 键，Flyway 配置合并至单一 spring 节点
- **修复** UserServiceImplTest 3处断言与安全加固实现不匹配（统一错误消息"手机号或密码错误"）
- **修复** DoctorServiceImplTest 5处断言与安全加固实现不匹配（同上）
- **修复** auth.test.ts 畸形JSON测试污染 localStorage mock 导致其他测试失败
- **新增** PrescriptionServiceImplTest（13个测试：处方创建/重复保护/权限/查看）
- **新增** PreConsultationServiceImplTest（10个测试：LLM配置校验/调用失败/响应解析/医生匹配/记录保存）
- **新增** ConsultationMessageServiceImplTest（7个测试：权限校验/分页/空结果/DTO映射）
- **新增** 前端测试：user.store（11）、pet.store（6）、validation（12）、request（8）、Prescription组件（13）
- **新增** Flyway V3 种子数据迁移（药品7种、地址、疫苗记录、通知示例）
- **更新** init.sql 部署路径说明（Flyway vs Docker entrypoint 双路径）
- **更新** PrescriptionServiceImpl 移除 TODO 注释
- **确认** package-lock.json 已纳入版本管理
- **修复** CORS 无效的 allowedHeaders(*) + allowCredentials(true) 组合 → 显式头部列表
- **修复** ConsultationController → 重构为 ConsultationService 层，修复医生无法查看问诊的授权缺陷
- **修复** EvaluationServiceImpl N+1 查询 → toEvaluationDTO 支持批量用户预加载
- **修复** README.md Spring Boot 版本标记 3.2.5→3.3.7，MyBatis-Plus 3.5.6→3.5.7
- **新增** AdminServiceImpl 首次登录强制修改密码检测（mustChangePassword 标志）
- **新增** Admin 修改密码端点 POST /api/admin/change-password
- **修复** WebSocket 重连竞态条件 — 使用 reconnectTimer 引用防止并发重连链
- **新增** Apache 2.0 所需的 NOTICE 文件
- **修复** docker-compose.yml 数据库/缓存端口安全建议，.env.example 生产端口提示
- **新增** 根级别 .dockerignore 文件
- **更新** nginx.conf CSP unsafe-inline 限制文档注释
- **清理** 根目录下的残留日志文件 (admin.log, server.log, web.log)
- **更新** application-prod.yml CORS 配置支持环境变量 CORS_ORIGIN_PATTERNS
- **修复** AESEncryptUtil 配置来源不一致 → 新增 AesConfig 桥接 Spring @Value("${aes.key}")
- **修复** 删除 V3__demo_seed_data.sql + application-prod.yml 禁用 Flyway 自动迁移
- **新增** JWT Token 黑名单撤销机制（RedisTokenBlacklistService + JwtAuthenticationFilter 检查）
- **新增** 用户端/管理端退出登录端点（POST /api/user/logout, /api/admin/logout），Token 加入黑名单
- **修复** 验证码暴力破解防护 — verifyAndConsumeCode 增加尝试次数限制（5次）
- **修复** API 全局限流 — 从固定窗口升级为滑动窗口算法（Redis Sorted Set）
- **修复** 手机号日志脱敏 — UserServiceImpl/DoctorServiceImpl/VerificationCodeServiceImpl
- **修复** DoctorServiceImpl.getDoctorDetail — 硬编码 LIMIT 5 → MyBatis-Plus Page
- **更新** README.md 测试覆盖描述更新为实际数量（9 后端 + 8 前端）
- **修复** docker-compose.yml 移除弱默认密码，使用 ${VAR:?err} 强制配置
- **删除** PetCare_Code_Review_Report.txt + petcare_schema.sql（过期文件）
- **修复** init.sql 种子数据安全警告增强 + 管理员密码 hash 统一为 admin123（修正注释不一致）
- **新增** AdminServiceImplTest（11 个测试：登录/首次登录检测/禁用检测/密码修改/旧密码校验）
- **修复** 管理后台 nginx.conf 缺少通用 `/api/` 代理路径
- **修复** 前端 Dockerfile nginx 容器改用非 root 用户（端口 8080）
- **清理** 根 package.json 移除残留的 bcryptjs 依赖
- **更新** README.md 测试覆盖描述更新为 10 后端 + 8 前端
- **修复** init.sql 注释明确 bcrypt hash 对应关系（$2a$10$N.zmdr9k7uOCQb376NoUnuT... = admin123）
- **新增** SECURITY.md（GitHub 安全策略 + 漏洞报告渠道 + 已知限制追踪）
- **修复** Dockerfile Maven dependency:go-offline \|\| true → dependency:resolve（避免掩盖依赖错误）
- **更新** nginx.conf CSP unsafe-inline 注释增加 GitHub Issue 追踪引用
- **新增** Vite manualChunks 代码分割（vendor-vue / vendor-element / vendor-axios 等）
- **优化** Vite chunkSizeWarningLimit=1200，构建输出零警告

### Planned — 计划中

- 微信/支付宝支付集成
- 阿里云/腾讯云短信 SDK 接入
- OSS 云存储集成（阿里云 OSS / 腾讯云 COS）
- JWT HttpOnly Secure Cookie + Refresh Token 模式
- 端到端测试（Playwright/Cypress）
- 国际化（i18n）
- 消息推送
