# auth-service/Dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# 1. Install spendsmart-common first
COPY spendsmart-common spendsmart-common
RUN cd spendsmart-common && mvn install -DskipTests -B --no-transfer-progress

# 2. Build auth-service
COPY auth-service auth-service
RUN cd auth-service && mvn package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring && apk add --no-cache curl
USER spring:spring
COPY --from=builder /app/auth-service/target/auth-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]