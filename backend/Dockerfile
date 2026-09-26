# Multi-stage build: compile with Maven+JDK, run on a slim JRE. Render builds this
# automatically when you point a Web Service at this directory with "Docker" as the
# environment — see /render.yaml at the repo root.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
