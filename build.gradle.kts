import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.moowork.gradle.node.npm.NpmTask
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("com.github.node-gradle.node") version "2.2.1"
}

group = "com.kotlinspringgraphlreact"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation(group = "com.expediagroup", name = "graphql-kotlin-schema-generator", version = "1.4.2")

    implementation(group = "com.graphql-java", name = "graphiql-spring-boot-starter", version = "5.0.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.register("webpack", NpmTask::class) {
    setNpmCommand("run", "webpack")
}

tasks.register("webpackDevelopment", NpmTask::class) {
    setNpmCommand("run", "webpack-development")
}

tasks.register("buildRelay", NpmTask::class) {
    setNpmCommand("run", "compile-relay")
}

tasks.getByName<BootRun>("bootRun") {
    dependsOn("npm_install", "webpack")
}

node {
    version = "12.16.0"
    npmVersion = "7.6.3"
    download = true
}
