# Multi-stage build for the ORBITLAB Spring Boot API
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/orbitlab-backend-0.1.0.jar /app/app.jar
ENV PORT=80
EXPOSE 80
ENTRYPOINT ["sh", "-c", "exec java -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-80} -jar /app/app.jar"]
