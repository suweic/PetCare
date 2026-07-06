# PetCare — 宠物在线问诊系统

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.3.7-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/vue-3.5-4FC08D.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/typescript-5.6-blue.svg)](https://www.typescriptlang.org/)

PetCare 是一个面向宠物主人的在线医疗问诊平台。用户可在线选择科室和医生，通过图文聊天进行远程问诊；医生可在线接诊、开具处方；平台提供 AI 预问诊辅助，支持全链路容器化部署。

---

## ✨ 核心功能

### 🏠 C端（宠物主人）

| 功能 | 说明 |
|------|------|
| **用户注册/登录** | 手机号 + 密码 / 短信验证码双模式 |
| **宠物管理** | 添加、编辑、查看宠物档案（品种、年龄、病史、过敏信息） |
| **医生浏览** | 科室筛选 + 搜索 + 分页，查看医生详情、评价、排班 |
| **AI 预问诊** | 描述宠物症状 → LLM 自动推荐科室和医生 |
| **在线问诊** | WebSocket 实时图文聊天，支持图片上传（≤5MB JPG/PNG） |
| **问诊记录** | 全部/进行中/已完成 Tab，支持查看历史消息 |
| **处方查看** | 处方卡片 + 详情（诊断、药品、用法用量、医嘱） |
| **评价医生** | 星级评分 + 文字评价 |

### 🔧 管理后台

| 功能 | 说明 |
|------|------|
| **数据看板** | 统计卡片 + ECharts 折线图/饼图/柱状图 |
| **医生审核** | 审核医生资质，通过/拒绝 + 审核意见 |
| **医生管理** | 搜索 + 列表 + 启用/禁用 |
| **用户管理** | 搜索 + 列表 + 禁用/启用 |
| **问诊记录** | 搜索 + 列表 + 详情弹窗 |
| **角色权限** | 超级管理员 / 运营管理员 / 审核员，仅管理员可登录后台 |

### 🤖 AI 预问诊

- 用户描述宠物症状
- 调用 LLM 进行症状分析
- 自动推荐匹配科室和医生
- 支持 OpenAI / Azure OpenAI / 国产大模型兼容接口

---

## 🏗️ 技术架构

```
┌──────────────────────────────────────────────────────┐
│                    Nginx (Docker)                     │
├──────────────────┬──────────────────┬────────────────┤
│   petcare-web    │  petcare-admin   │   Uploads/     │
│   (Vue 3 SPA)    │   (Vue 3 SPA)    │   WebSocket    │
│   Port: 8082     │   Port: 8081     │                │
└──────────────────┴──────────────────┴────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │   petcare-server      │
              │   Spring Boot 3.3.7   │
              │   Java 17 + JWT       │
              │   Port: 8080          │
              └───────────┬───────────┘
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
      ┌──────────────┐       ┌──────────────┐
      │  MySQL 8.0   │       │   Redis 7    │
      │  Port: 3306  │       │  Port: 6379  │
      └──────────────┘       └──────────────┘
```

### 技术栈

| 层级 | 技术 |
|------|------|
| **后端框架** | Spring Boot 3.3.7 |
| **语言** | Java 17 |
| **ORM** | MyBatis-Plus 3.5.7 |
| **安全** | Spring Security + JWT + AES-256-GCM |
| **数据库** | MySQL 8.0（25 张表，外键约束，utf8mb4） |
| **缓存** | Redis 7（JWT 黑名单、验证码、限流计数） |
| **前端** | Vue 3.5 + Composition API + TypeScript |
| **UI 框架** | Element Plus 2.9 + Element Plus Icons |
| **状态管理** | Pinia 2.3 |
| **路由** | Vue Router 4.5 |
| **图表** | ECharts 5.6 |
| **实时通信** | WebSocket（STOMP/SockJS） |
| **构建工具** | Vite 6（前端）、Maven（后端） |
| **容器化** | Docker + Docker Compose（Nginx + 多阶段构建） |

---

## 🚀 快速开始

### 前置条件

- Docker 24+ & Docker Compose v2+
- （本地开发）Node.js 20+、Java 17+、Maven 3.9+

### Docker 一键部署

```bash
# 1. 克隆项目
git clone <your-repo-url> petcare
cd petcare

# 2. 生成密钥并配置环境变量
openssl rand -base64 32  # 生成 JWT_SECRET
openssl rand -base64 32  # 生成 AES_KEY
openssl rand -base64 16  # 生成 DB_PASSWORD
openssl rand -base64 12  # 生成 REDIS_PASSWORD

# 3. 编辑 .env，填入生成的密钥
cp .env.example .env
vim .env

# 4. 启动所有服务
docker compose up -d

# 5. 访问
#    用户端: http://localhost:8082
#    管理后台: http://localhost:8081
#   后端 API: http://localhost:8080
```

首次启动会自动初始化数据库（执行 `init.sql`，仅包含 24 张表的 DDL 结构）。
如需演示种子数据，请参考 [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) 中的说明。

### 本地开发

#### 后端

```bash
cd petcare-server
mvn clean install -DskipTests
cd petcare-system
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 用户端前端

```bash
cd petcare-web
npm install
npm run dev          # 默认 http://localhost:5173
```

#### 管理后台前端

```bash
cd petcare-admin
npm install
npm run dev          # 默认 http://localhost:5174
```

---

## 📂 项目结构

```
petcare/
├── petcare-server/               # Spring Boot 后端
│   ├── petcare-common/           # 公共模块（异常、响应、工具类）
│   ├── petcare-security/         # 安全模块（JWT、CORS、Spring Security）
│   ├── petcare-system/           # 业务模块（Controller、Service、Mapper、Entity）
│   │   └── src/main/
│   │       ├── java/com/petcare/system/
│   │       │   ├── config/       # 配置类
│   │       │   ├── controller/   # 控制器（用户端 + 管理端）
│   │       │   ├── dto/          # 数据传输对象
│   │       │   ├── entity/       # 数据库实体
│   │       │   ├── enums/        # 枚举
│   │       │   ├── mapper/       # MyBatis Mapper
│   │       │   ├── service/      # 服务接口 + 实现
│   │       │   └── converter/    # MapStruct 转换器
│   │       └── resources/
│   │           ├── mapper/       # MyBatis XML
│   │           ├── application.yml
│   │           ├── application-dev.yml
│   │           └── application-prod.yml
│   └── Dockerfile
├── petcare-web/                  # Vue 3 用户端
│   └── src/
│       ├── api/                  # API 封装
│       ├── assets/               # 静态资源
│       ├── components/           # 通用组件
│       ├── layouts/              # 布局组件
│       ├── router/               # Vue Router
│       ├── stores/               # Pinia 状态管理
│       ├── types/                # TypeScript 类型
│       ├── utils/                # 工具函数
│       └── views/                # 页面视图
├── petcare-admin/                # Vue 3 管理后台
│   └── src/
│       ├── api/                  # API 封装
│       ├── router/               # Vue Router + 角色守卫
│       ├── stores/               # Pinia 状态管理
│       ├── types/                # TypeScript 类型
│       ├── utils/                # 工具函数
│       └── views/                # 页面（Dashboard、审核、管理）
├── init.sql                      # 数据库初始化脚本（24 张表 DDL）
├── docker-compose.yml            # Docker Compose 编排（MySQL + Redis + Server + Web + Admin）
├── .env.example                  # 环境变量模板
├── .env                          # 实际环境变量（已 gitignored）
├── DEPLOYMENT_GUIDE.md           # 生产部署指南
└── LICENSE                       # Apache 2.0
```

---

## 🔒 安全特性

- **认证**：JWT 无状态认证，双 SecurityFilterChain（用户端 + 管理端）
- **加密**：AES-256-GCM 加密身份证等敏感个人信息（个人信息保护法合规）
- **限流**：Redis 实现全局限流（20 req/s per IP）
- **防暴力破解**：5 次/15 分钟登录锁定（Redis 计数）
- **文件上传安全**：魔术字节（Magic Bytes）校验、Content-Type 白名单、路径遍历防护、10MB 上限
- **角色授权**：管理员后台 `userType=3` 路由守卫 + 后端双重校验
- **逻辑删除**：用户/医生/问诊等核心实体使用 `deleted` 字段

---

## 📊 数据库

共 25 张表（24 张在 init.sql + 1 张 Flyway 迁移 `refresh_token`），核心表：

| 表名 | 说明 |
|------|------|
| `user` | 用户表（支持多角色：普通用户/医生/管理员） |
| `pet` | 宠物档案表 |
| `doctor` | 医生信息表 |
| `doctor_audit_log` | 医生审核日志 |
| `department` | 科室表 |
| `consultation` | 问诊记录表 |
| `consultation_message` | 问诊消息表 |
| `prescription` | 处方表 |
| `prescription_item` | 处方药品明细表 |
| `medicine` | 药品目录表 |
| `evaluation` | 评价表 |
| `doctor_schedule` | 医生排班表 |
| `order` | 订单表 |
| `verification_code` | 短信验证码（Redis 为主） |
| `pre_consultation` | AI 预问诊记录表 |

---

## 📝 环境变量

参见 `.env.example` 文件。关键环境变量：

| 变量 | 说明 |
|------|------|
| `JWT_SECRET` | JWT 签名密钥（至少 32 字符 Base64） |
| `AES_KEY` | AES-256-GCM 加密密钥（32 字节 Base64） |
| `DB_PASSWORD` | MySQL 应用密码 |
| `DB_ROOT_PASSWORD` | MySQL root 密码 |
| `REDIS_PASSWORD` | Redis 密码 |
| `LLM_API_URL` | LLM API 地址（AI 预问诊） |
| `LLM_API_KEY` | LLM API 密钥 |
| `LLM_MODEL` | LLM 模型名称（默认 gpt-4o-mini） |

---

## 🧪 测试

当前测试覆盖：

- **后端**：10 个测试类（JwtUtilsTest、AdminServiceImplTest、DoctorServiceImplTest、UserServiceImplTest、PetServiceImplTest、PrescriptionServiceImplTest、PreConsultationServiceImplTest、ConsultationMessageServiceImplTest、DepartmentServiceImplTest、EvaluationServiceImplTest），约 80+ 测试用例
- **前端**：8 个测试文件（petcare-web: user.store、pet.store、validation、request、upload、Prescription 组件、auth；petcare-admin: auth）

> 完善测试是项目的优先级工作，欢迎贡献测试用例！

---

## 📄 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源。

---

## 📮 联系方式

- Issues: 请通过 GitHub Issues 提交
- 部署问题请先查阅 [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
