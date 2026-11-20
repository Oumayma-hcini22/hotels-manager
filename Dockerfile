# ---- 1. Compilation du projet avec Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ---- 2. Image finale : Java 17 + psm.jar ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/psm.jar psm.jar
EXPOSE 8084
# memoire limitee sur l'offre gratuite de Render (512 Mo)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-XX:+UseSerialGC", "-Xss512k", "-jar", "psm.jar"]