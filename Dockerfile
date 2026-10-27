# Multi-stage Dockerfile for Spring Boot 4 / Java 21 Backend
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy gradle wrapper and build definitions first for layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Fix execute permissions on gradlew
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build executable jar
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar from builder
COPY --from=builder /workspace/build/libs/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

# Environment variables with sensible defaults
ENV SPRING_PROFILES_ACTIVE=shared \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
