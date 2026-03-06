# syntax=docker/dockerfile:1

############################
# 1️⃣ Build stage
############################
FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven wrapper + pom first (better caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source
COPY src/ src/

# Build jar
RUN ./mvnw -q -DskipTests package


############################
# 2️⃣ Runtime stage
############################
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose Spring Boot port
EXPOSE 8082

# Run application
ENTRYPOINT ["java","-jar","/app/app.jar"]