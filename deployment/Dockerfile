# Stage 1: Build the application
FROM gradle:8.7-jdk22 AS builder

WORKDIR /app

# Copy the Gradle wrapper files and build scripts
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .

# Copy the source code
COPY src src

# Build the Spring Boot application
RUN ./gradlew bootJar

# Stage 2: Run the application
FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

# Create a non-root user and group
RUN addgroup --system spring && adduser --system --ingroup spring spring

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Set the user for running the application
USER spring

# Expose the port your Spring Boot application runs on (e.g., 8080)
EXPOSE 8081

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]