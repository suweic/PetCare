# ===== Stage 1: Node 构建 =====
FROM node:20-alpine AS builder

WORKDIR /app
COPY package.json ./
RUN npm install --registry=https://registry.npmmirror.com

COPY tsconfig*.json vite.config.ts index.html env.d.ts ./
COPY src/ src/

RUN npm run build

# ===== Stage 2: Nginx 托管 =====
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:80/ || exit 1
