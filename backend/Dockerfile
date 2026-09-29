# 1. Use Java 21 with Maven included for development environment
FROM maven:3.9.6-eclipse-temurin-21

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Copy only the pom.xml first to leverage Docker layer caching
# This prevents re-downloading dependencies if only source code changes
COPY pom.xml .

# 4. Download dependencies to the local repository (offline mode)
RUN mvn dependency:go-offline -B

# 5. Source code will be mounted via VOLUMES in docker-compose.yml
# This allows real-time development and hot-reloading.

# 6. Expose the default Spring Boot port
EXPOSE 8080

# 7. Command to run the application in development mode
# This compiles and starts the app, watching for changes
CMD ["mvn", "spring-boot:run"]