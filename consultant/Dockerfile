# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 先拷贝 pom.xml 利用缓存
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 再拷贝源码
COPY src ./src

# 构建 Spring Boot jar
RUN mvn -B -q clean package -DskipTests


# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# JVM 参数（容器友好）
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 拷贝 jar
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# 启动
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
