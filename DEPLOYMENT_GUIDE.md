# PetCare 生产环境部署指南

> 本文档列出了将 PetCare 系统部署到生产环境前需要手动完成的所有操作步骤。
> 代码层面能自动修复的问题已在 2026-07-01 完成，以下为需要人工操作的事项。

---

## 前置条件检查清单

在开始部署前，请逐项确认：

- [ ] 拥有一台 Linux 服务器（推荐 Ubuntu 22.04 / CentOS 8+，2核4G以上）
- [ ] 已购买域名并完成备案（国内服务器必需）
- [ ] 服务器已安装 Docker 24+ 和 Docker Compose v2+
- [ ] 已配置安全组/防火墙规则（开放 80, 443 端口）
- [ ] 已从代码仓库拉取最新代码

---

## 第一步：生成密钥并配置环境变量 ⚠️ 最关键

### 1.1 在服务器上执行以下命令生成密钥

```bash
# 生成 JWT 签名密钥（32 字节 Base64）
echo "JWT_SECRET:"
openssl rand -base64 32

# 生成 AES 加密密钥（32 字节 Base64）
echo "AES_KEY:"
openssl rand -base64 32

# 生成 Redis 密码（16 字节随机字符串）
echo "REDIS_PASSWORD:"
openssl rand -base64 16

# 生成 MySQL root 密码
echo "DB_ROOT_PASSWORD:"
openssl rand -base64 16

# 生成 MySQL 应用密码
echo "DB_PASSWORD:"
openssl rand -base64 16
```

### 1.2 将生成的值填入 .env 文件

```bash
cd /path/to/petcare
cp .env.example .env
vim .env  # 修改以下字段为刚才生成的值
```

需要修改的字段：
```
JWT_SECRET=<上一步生成的 JWT_SECRET>
AES_KEY=<上一步生成的 AES_KEY>
REDIS_PASSWORD=<上一步生成的 REDIS_PASSWORD>
DB_ROOT_PASSWORD=<上一步生成的 DB_ROOT_PASSWORD>
DB_PASSWORD=<上一步生成的 DB_PASSWORD>
```

> ⚠️ **绝对不要使用 .env 中的默认值！** 代码仓库中的密钥是公开的，任何人拿到都能伪造 Token。

---

## 第二步：修改默认管理员密码

### 方案 A：部署后通过后台修改（推荐）
1. 启动服务后访问 `http://<域名>:8081/login`
2. 使用默认账号登录：`admin` / `admin123`
3. 进入个人中心 → 修改密码 → 输入新密码

### 方案 B：部署前替换 bcrypt 哈希
1. 生成新密码的 bcrypt 哈希：
```bash
# 使用 Python
python3 -c "import bcrypt; print(bcrypt.hashpw(b'YOUR_NEW_PASSWORD', bcrypt.gensalt()).decode())"

# 或使用在线工具 https://www.bcryptcalculator.com/
```
2. 编辑 `init.sql`，替换 admin 记录的 password 字段值

---

## 第三步：配置 HTTPS（SSL 证书）

### 推荐方案：Nginx Proxy Manager + Let's Encrypt

```bash
# 1. 安装 Nginx Proxy Manager
mkdir -p /opt/nginx-proxy-manager
cd /opt/nginx-proxy-manager
cat > docker-compose.yml << 'EOF'
version: '3.8'
services:
  app:
    image: jc21/nginx-proxy-manager:latest
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
      - "81:81"
    volumes:
      - ./data:/data
      - ./letsencrypt:/etc/letsencrypt
EOF
docker compose up -d
```

2. 访问 `http://<服务器IP>:81`，默认账号 `admin@example.com` / `changeme`

3. 在 NPM 管理界面：
   - 添加 SSL 证书（Let's Encrypt），填写你的域名
   - 添加 Proxy Host：
     - 用户端：`web.yourdomain.com` → `petcare-web:80`
     - 管理后台：`admin.yourdomain.com` → `petcare-admin:80`
     - API：`api.yourdomain.com` → `petcare-server:8080`
   - 每个都勾选 SSL 并选择刚创建的证书

### 备选方案：直接在 petcare 的 nginx 配置 SSL

如果不想引入 NPM，可以在 `petcare-web/nginx.conf` 和 `petcare-admin/nginx.conf` 中添加 SSL 配置：

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate     /etc/ssl/certs/your-cert.pem;
    ssl_certificate_key /etc/ssl/private/your-key.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;
    
    # ... 其他配置保持不变
}

server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$host$request_uri;  # HTTP → HTTPS 重定向
}
```

---

## 第四步：对接短信服务（验证码功能）

当前验证码仅存储在 Redis，未实际发送。生产环境需要对接短信服务商。

### 推荐：阿里云短信服务

1. **开通服务**
   - 登录 [阿里云短信服务控制台](https://dysms.console.aliyun.com/)
   - 申请短信签名（如"PetCare宠物问诊"）
   - 申请短信模板（验证码模板：`您的验证码是${code}，5分钟内有效`）

2. **获取凭证**
   - AccessKey ID
   - AccessKey Secret

3. **修改代码**
   编辑 `VerificationCodeServiceImpl.java` 的 `sendCode()` 方法：
   ```java
   // 在第 48 行 TODO 位置添加：
   SmsClient client = SmsClient.builder()
       .accessKeyId(accessKeyId)
       .accessKeySecret(accessKeySecret)
       .build();
   SendSmsRequest request = SendSmsRequest.builder()
       .phoneNumbers(phone)
       .signName("PetCare宠物问诊")
       .templateCode("SMS_XXXXXXXXX")
       .templateParam("{\"code\":\"" + code + "\"}")
       .build();
   client.sendSms(request);
   ```

4. **添加依赖**
   在 `petcare-system/pom.xml` 中添加：
   ```xml
   <dependency>
       <groupId>com.aliyun</groupId>
       <artifactId>dysmsapi20170525</artifactId>
       <version>3.0.0</version>
   </dependency>
   ```

### 国内备选：腾讯云短信
类似流程，使用 `tencentcloud-sdk-java` 依赖。

### 国际备选：Twilio
适合海外部署，使用 `twilio` Java SDK。

---

## 第五步：配置文件存储（图片/证书上传）

### 开发环境（已就绪）
文件存储在 `./uploads/` 本地目录，通过 Spring Boot 静态资源映射和 nginx 代理访问。

### 生产环境推荐：阿里云 OSS / 腾讯云 COS / AWS S3

**以阿里云 OSS 为例：**

1. 开通 OSS 服务，创建 Bucket（如 `petcare-uploads`）
2. 获取 AccessKey 和 Bucket 域名
3. 修改 `FileUploadController.java`，将本地存储替换为 OSS 上传：

```java
// 替换 Files.copy() 部分：
OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
ossClient.putObject(bucketName, objectKey, file.getInputStream());
String fileUrl = "https://" + bucketName + "." + endpoint + "/" + objectKey;
```

4. 添加 OSS SDK 依赖到 pom.xml
5. 配置 CORS（在 OSS 控制台中设置允许的跨域来源）

---

## 第六步：数据库备份策略

### 自动备份脚本

在服务器上创建 `/opt/scripts/backup-petcare.sh`：

```bash
#!/bin/bash
BACKUP_DIR="/opt/backups/petcare"
RETENTION_DAYS=30
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 导出数据库
docker exec petcare-mysql mysqldump \
  -u root -p${DB_ROOT_PASSWORD} \
  --single-transaction \
  --routines \
  --triggers \
  petcare | gzip > ${BACKUP_DIR}/petcare_${TIMESTAMP}.sql.gz

# 删除旧备份（保留最近 30 天）
find ${BACKUP_DIR} -name "petcare_*.sql.gz" -mtime +${RETENTION_DAYS} -delete

echo "[$(date)] 备份完成: petcare_${TIMESTAMP}.sql.gz"
```

设置定时任务：
```bash
chmod +x /opt/scripts/backup-petcare.sh
# 每天凌晨 3 点备份
(crontab -l 2>/dev/null; echo "0 3 * * * /opt/scripts/backup-petcare.sh >> /var/log/petcare-backup.log 2>&1") | crontab -
```

---

## 第七步：配置日志收集

### 推荐：Docker 日志驱动 + Loki + Grafana

**简易方案（直接可部署）：**

在 `docker-compose.yml` 的服务中添加：
```yaml
services:
  server:
    # ... 原有配置 ...
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "5"
```

这样 Docker 会自动轮转日志，保留最近 5 个 100MB 的文件。

**进阶方案：ELK Stack**
```bash
# 添加 Elasticsearch + Logstash + Kibana 到 docker-compose
# 或将日志转发到云服务（阿里云 SLS、腾讯云 CLS）
```

---

## 第八步：配置 CI/CD（可选但推荐）

### GitHub Actions 示例

在项目根目录创建 `.github/workflows/deploy.yml`：

```yaml
name: Deploy PetCare
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Build and push Docker images
        run: |
          docker compose build
          docker tag petcare-server:latest registry.yourdomain.com/petcare-server:${{ github.sha }}
          docker push registry.yourdomain.com/petcare-server:${{ github.sha }}
      
      - name: Deploy to server
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: deploy
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd /opt/petcare
            docker compose pull
            docker compose up -d --no-deps server admin web
```

需要配置的 GitHub Secrets：
- `SERVER_HOST`: 服务器 IP
- `SSH_PRIVATE_KEY`: SSH 私钥

---

## 第九步：监控和告警

### 健康检查（已配置）
Docker Compose 中已配置健康检查，可通过以下命令查看：
```bash
docker compose ps
```

### Uptime 监控（推荐）
- 使用 [UptimeRobot](https://uptimerobot.com/)（免费 50 个监控）
- 监控 URL：`https://api.yourdomain.com/actuator/health`

### 应用性能监控（可选）
- 集成 Spring Boot Actuator + Micrometer + Prometheus + Grafana
- 或使用云服务：阿里云 ARMS、腾讯云 APM

---

## 第十步：上线前最终检查清单

### 安全检查
- [ ] `.env` 中所有密钥已替换为生产唯一值
- [ ] 默认管理员密码已修改（`admin123` → 强密码）
- [ ] HTTPS 证书已配置且有效
- [ ] 防火墙仅开放 80/443 端口（不暴露 3306/6379/8080）
- [ ] Redis 已设置密码（`REDIS_PASSWORD` 不为空）
- [ ] MySQL root 密码已修改
- [ ] 身份证号加密密钥 `AES_KEY` 已配置

### 功能检查
- [ ] 用户注册/登录流程正常
- [ ] 医生注册 → 审核 → 接诊流程正常
- [ ] 问诊创建 → 消息收发 → 处方开具流程正常
- [ ] 评价创建后医生评分正确更新
- [ ] 文件上传（头像、证书、问诊图片）正常
- [ ] WebSocket 连接 / 重连正常
- [ ] 消息历史分页查询正常

### 性能检查
- [ ] `docker compose up -d` 所有容器正常启动
- [ ] 数据库连接池参数已按生产调整（application-prod.yml 中默认 20）
- [ ] API 响应时间在可接受范围内
- [ ] 静态资源缓存头正确（nginx 已配置 `/assets/` 1年缓存）

### 运维检查
- [ ] 数据库自动备份脚本已部署并测试恢复
- [ ] 日志轮转策略已配置
- [ ] 服务重启策略为 `unless-stopped`
- [ ] 已记录所有密钥的安全备份位置

---

## 快速启动命令

完成以上配置后，按以下命令启动生产环境：

```bash
# 1. 进入项目目录
cd /opt/petcare

# 2. 确认环境变量
cat .env  # 确认所有密钥已替换

# 3. 构建并启动所有服务
docker compose up -d --build

# 4. 查看启动状态
docker compose ps

# 5. 查看日志
docker compose logs -f server

# 6. 验证健康检查
curl http://localhost:8080/actuator/health

# 7. 访问服务
#   用户端:  http://<域名>:8082
#   管理后台: http://<域名>:8081
#   API:     http://<域名>:8080
```

## 应急预案

| 问题 | 处理方式 |
|------|---------|
| 数据库误删 | 从最近备份恢复：`gunzip < backup.sql.gz \| docker exec -i petcare-mysql mysql -u root -p petcare` |
| Token 泄露 | 立即更换 JWT_SECRET，重启 server 容器，所有用户需重新登录 |
| 磁盘空间不足 | 清理 Docker 日志：`docker system prune -a --volumes -f` |
| 服务不可用 | 回滚到上一个正常版本：`docker compose up -d --no-deps server admin web`（如果使用了版本标签） |

---

> **文档版本**: 1.0  
> **最后更新**: 2026-07-01  
> **请将此文档与项目代码一起维护，随版本迭代更新**
