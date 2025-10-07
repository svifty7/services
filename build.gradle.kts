plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.svifty7"
version = "0.0.1-SNAPSHOT"
description = "services"

val isDevelopment: Boolean = project.hasProperty("dev")
val isLocal: Boolean = project.hasProperty("local")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

springBoot {
    mainClass.set("ru.svifty7.services.ServicesApplication")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Инфраструктура
    implementation("org.liquibase:liquibase-core")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation("io.jsonwebtoken:jjwt:0.12.5")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.9.2")
    implementation("org.springframework.retry:spring-retry:2.0.11")
    implementation("org.springframework:spring-aspects:6.2.11")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.3.0")
    implementation("commons-io:commons-io:2.18.0")
    implementation("com.vk.api:sdk:1.0.14")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    compileOnly("org.mapstruct:mapstruct-processor:1.6.3")

    // Annotation processors
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Runtime-only
    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
}
