# ===== Stage 1: Maven 构建 =====
# 使用 Debian 基础镜像（非 Alpine），避免 musl libc 网络栈兼容问题
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY petcare-common/pom.xml petcare-common/
COPY petcare-security/pom.xml petcare-security/
COPY petcare-system/pom.xml petcare-system/

# 先下载依赖（利用 Docker 缓存层）
# --fail-at-end: 即使有错误也下载所有依赖再报告
# -Dmaven.wagon.http.retryHandler.count=3: 网络失败自动重试
RUN mvn dependency:resolve -pl petcare-system -am -B --fail-at-end \
    -Dmaven.wagon.http.retryHandler.count=3

COPY petcare-common/src petcare-common/src
COPY petcare-security/src petcare-security/src
COPY petcare-system/src petcare-system/src

RUN mvn package -pl petcare-system -am -DskipTests -B --fail-at-end \
    -Dmaven.wagon.http.retryHandler.count=3

# ===== Stage 2: 运行镜像 =====
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -g 1000 petcare && adduser -u 1000 -G petcare -D petcare
USER petcare

WORKDIR /app
COPY --from=builder /build/petcare-system/target/petcare-system-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", "app.jar"]
