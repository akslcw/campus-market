FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY docker/maven-settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/target/market-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/upload

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
