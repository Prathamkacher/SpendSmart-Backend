# budget-service/Dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY spendsmart-common spendsmart-common
RUN cd spendsmart-common && mvn install -DskipTests -B --no-transfer-progress
COPY budget-service budget-service
RUN cd budget-service && mvn package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring && apk add --no-cache curl
USER spring:spring
COPY --from=build /app/budget-service/target/budget-service-1.0.0.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
