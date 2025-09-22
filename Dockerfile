# Build stage
FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace
COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle gradle
COPY src src
RUN gradle clean bootJar -x test

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
