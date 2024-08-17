plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("plugin.jpa") version "1.9.24"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
}

group = "com.yuiyeong"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // for kafka
    implementation("org.springframework.kafka:spring-kafka")

    // for restarting app when code changes
    implementation("org.springframework.boot:spring-boot-devtools")

    // for mysql
    runtimeOnly("com.mysql:mysql-connector-j")

    // for redis
    implementation("org.redisson:redisson-spring-boot-starter:3.34.1")

    // for jwt
    api("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-orgjson:0.12.6")

    // for cache
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // for swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // for logging
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0")

    // for metric
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // for slack
    implementation("com.slack.api:slack-api-client:1.40.3")

    /**************************** For Test ****************************/
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    // for testcontainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:kafka")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
