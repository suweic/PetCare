# ===== Stage 1: Maven 构建 =====
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY petcare-common/pom.xml petcare-common/
COPY petcare-security/pom.xml petcare-security/
COPY petcare-system/pom.xml petcare-system/
COPY petcare-common/src petcare-common/src
COPY petcare-security/src petcare-security/src
COPY petcare-system/src petcare-system/src

# 一步完成依赖解析+编译+打包，避免分层缓存不一致
RUN mvn package -pl petcare-system -am -DskipTests -B

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
