# Stage 1: Build the application using a Maven image
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn ./.mvn
RUN ./mvnw -B -V -e clean package -DskipTests

# Stage 2: Create the final, lightweight image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar
# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080
# The command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]
