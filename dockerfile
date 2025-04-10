# Build stage (JDK 21 with Gradle)
FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /app

# Copy Gradle files (layer caching optimization)
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

# Build the app (caches Gradle dependencies)
RUN ./gradlew clean build -x test --no-daemon

# Runtime stage (JRE 21 - smaller image)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy built JAR (adjust path if using custom jar name)
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar

# Railway-specific config
ENV PORT=8080
EXPOSE $PORT

# Optimized JVM for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Entrypoint with dynamic port support
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
