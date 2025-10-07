FROM eclipse-temurin:25 AS build
WORKDIR /opt/app
COPY --link . .
RUN ./gradlew clean build

FROM eclipse-temurin:25-jre-alpine
WORKDIR /opt/app
ENV TZ=UTC
COPY --from=build /opt/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
