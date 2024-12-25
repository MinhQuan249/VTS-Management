# Stage 1: Build backend app
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Sao chép pom.xml và tải dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Sao chép mã nguồn vào container
COPY src ./src

# Build ứng dụng
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim
WORKDIR /app

# Sao chép file JAR từ giai đoạn build
COPY --from=builder /app/target/*.jar app.jar

# Khai báo cổng
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
