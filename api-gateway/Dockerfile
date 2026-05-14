# api-gateway/Dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 1. Install spendsmart-common
COPY spendsmart-common spendsmart-common
RUN cd spendsmart-common && mvn install -DskipTests -B --no-transfer-progress

# 2. Build api-gateway
COPY api-gateway api-gateway
RUN cd api-gateway && mvn package -DskipTests -B --no-transfer-progress

# ── Stage 2: Run ───────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring && apk add --no-cache curl
USER spring:spring
COPY --from=build /app/api-gateway/target/api-gateway-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
