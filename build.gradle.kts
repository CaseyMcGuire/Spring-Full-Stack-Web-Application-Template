import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.moowork.gradle.node.npm.NpmTask
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
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
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.springframework.boot:spring-boot-starter-thymeleaf")

    compile(group = "com.expediagroup", name = "graphql-kotlin-schema-generator", version = "1.4.2")

    compile(group = "com.graphql-java", name = "graphiql-spring-boot-starter", version = "5.0.2")

    compile("org.jetbrains.kotlin:kotlin-reflect:1.3.0") // https://stackoverflow.com/a/47174551/11283051
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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

tasks

node {
    version = "12.16.0"
    npmVersion = "7.6.3"
    download = true
}