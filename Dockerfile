
FROM gradle:jdk21 as builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon


FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]