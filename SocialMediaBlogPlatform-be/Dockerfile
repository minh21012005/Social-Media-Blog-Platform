# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy the entire project
COPY . .

# Build argument to specify which module to build (e.g. services/user-service)
ARG MODULE_PATH

# Build only the specified module and its dependencies (like common)
RUN mvn clean package -pl ${MODULE_PATH} -am -Dmaven.test.skip=true

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ARG MODULE_PATH

# Copy the built jar from the builder stage
COPY --from=builder /app/${MODULE_PATH}/target/*.jar app.jar

# Copy configuration files (like JWT keys)
COPY config/ config/

ENTRYPOINT ["java", "-jar", "app.jar"]
