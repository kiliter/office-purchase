# 演示镜像：把 Vue 页面打进 Spring Boot，运行时只启用 H2，不连接 MySQL。

FROM node:20-bookworm-slim AS web
WORKDIR /src/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend ./
# Vite 的输出目录在 frontend 的上一级，先把目录建出来
RUN mkdir -p /src/backend/src/main/resources && npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
COPY --from=web /src/backend/src/main/resources/static backend/src/main/resources/static
WORKDIR /src/backend
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /src/backend/target/purchase-1.0.0.jar /app/app.jar
EXPOSE 8080
# Spring Boot 会读取这个环境变量，固定走 H2 演示配置
ENV SPRING_PROFILES_ACTIVE=h2
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
