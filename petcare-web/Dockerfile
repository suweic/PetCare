# ===== Stage 1: Node 构建 =====
FROM node:20-alpine AS builder

# NPM 镜像源（可通过 --build-arg NPM_REGISTRY=... 自定义）
ARG NPM_REGISTRY=https://registry.npmjs.org

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --registry=${NPM_REGISTRY}

COPY tsconfig*.json vite.config.ts index.html env.d.ts ./
COPY src/ src/

RUN npm run build

# ===== Stage 2: Nginx 托管（非 root 用户）=====
FROM nginx:1.27-alpine

# 使用非 root 端口 8080，避免特权端口绑定
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

# 设置 nginx 运行目录权限
RUN chown -R nginx:nginx /usr/share/nginx/html /var/cache/nginx /var/run && \
    mkdir -p /var/log/nginx && chown -R nginx:nginx /var/log/nginx

USER nginx

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/ || exit 1
