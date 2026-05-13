# notification-service/Dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 1. Install spendsmart-common
COPY spendsmart-common spendsmart-common
RUN cd spendsmart-common && mvn install -DskipTests -B --no-transfer-progress

# 2. Build notification-service
COPY notification-service notification-service
RUN cd notification-service && mvn package -DskipTests -B --no-transfer-progress

# ── Stage 2: Run ───────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring && apk add --no-cache curl
USER spring:spring
COPY --from=build /app/notification-service/target/notification-service-1.0.0.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
