# 安全策略

## 支持的版本

| 版本 | 支持状态 |
|------|----------|
| 1.x  | ✅ 活跃支持 |

## 报告漏洞

PetCare 项目高度重视安全问题。如果你发现了安全漏洞，**请不要在公开 Issue 中报告**。

请通过以下方式私密报告：

- **邮件**: [2733808428@qq.com](mailto:2733808428@qq.com)（推荐 PGP 加密）
- **GitHub Security Advisory**: 在仓库页面点击 "Security" → "Report a vulnerability"

我们会在 **48 小时内确认收到报告**，并在 **7 天内提供初步评估和修复时间表**。

### 漏洞报告要求

请包含以下信息以便我们快速定位和修复：

1. 漏洞类型（如 XSS、SQL 注入、认证绕过、信息泄露等）
2. 受影响的版本号和组件
3. 复现步骤或 PoC 代码
4. 潜在影响评估
5. 建议的修复方案（如有）

## 安全最佳实践

### 生产部署

1. **密钥管理**: 使用 `openssl rand -base64 32` 生成所有密钥，分别填入 `.env` 文件
2. **数据库**: 生产环境不要暴露 3306/6379 端口
3. **HTTPS**: 必须配置 SSL/TLS 证书（推荐 Let's Encrypt）
4. **Flyway**: 生产环境 `application-prod.yml` 已禁用自动迁移
5. **演示数据**: `init.sql` 仅包含 DDL（表结构），不含种子数据。如需演示数据，请使用 `init-seed.sql`（已 gitignored，需手动创建）
6. **首次登录**: 管理员登录后应立即修改默认密码

### Token 存储

当前使用 localStorage 存储 JWT Token。项目已记录迁移到 HttpOnly Cookie 方案的路径（参见 `petcare-web/src/utils/auth.ts`）。

## 已知限制

| 限制 | 影响 | 缓解措施 | 追踪 |
|------|------|----------|------|
| CSP `style-src 'unsafe-inline'` | 允许内联样式，削弱 CSP 防护 | Element Plus 兼容性要求；生产建议 nonce 方案 | Issue #待创建 |
| Token localStorage | XSS 可窃取 Token | Nginx CSP + SecurityConfig X-Frame-Options + X-Content-Type-Options 三层防护 | 计划迁移 HttpOnly Cookie |
| 短信仅控制台模拟 | 未实际发送短信 | `ConsoleSmsServiceImpl` 标记为 mock；生产需实现 `SmsService` | DEPLOYMENT_GUIDE.md §第四步 |

## 依赖安全

项目使用以下安全相关依赖：

| 依赖 | 用途 | 版本 |
|------|------|------|
| Spring Security | 认证/授权框架 | 6.x (Spring Boot 3.3.7) |
| JJWT | JWT 签发/验证 | 0.12.5 |
| Bcrypt (Spring Security) | 密码哈希 | 内置于 Spring Security |
| AES-256-GCM (JCA) | 敏感信息加密 | JDK 内置 |

建议在 CI 中集成依赖漏洞扫描（OWASP Dependency-Check 或 Snyk）。

---

*最后更新: 2026-07-06*
