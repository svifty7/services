FROM maven:3-eclipse-temurin-25 AS base

WORKDIR /opt/app


FROM base AS build

COPY --link . .

RUN mvn -B clean install -DskipTests


FROM eclipse-temurin:25-jre

WORKDIR /opt/app

COPY --from=build /opt/app/target/services.jar services.jar

ENTRYPOINT ["java","-jar","services.jar"]
