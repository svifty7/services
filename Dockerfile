FROM eclipse-temurin:25 AS build
WORKDIR /opt/app
COPY --link gradle gradle
COPY --link gradlew build.gradle.kts settings.gradle.kts ./
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon
COPY --link src src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew build -x test --no-daemon --parallel

FROM eclipse-temurin:25-jre-alpine
WORKDIR /opt/app
ENV TZ=UTC
COPY --from=build /opt/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
