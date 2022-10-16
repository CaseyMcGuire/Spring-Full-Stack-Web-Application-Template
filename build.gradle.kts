import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.gradle.node.npm.task.NpmTask
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    id("org.springframework.boot") version "2.7.4"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("com.github.node-gradle.node") version "3.4.0"
    id("com.netflix.dgs.codegen") version "5.2.4"
}

group = "com.kotlinspringgraphlreact"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:latest.release"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
    implementation(group = "com.graphql-java", name = "graphiql-spring-boot-starter", version = "5.0.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.register<NpmTask>("webpack") {
    npmCommand.set(listOf("run", "webpack"))
}

tasks.register<NpmTask>("webpackDevelopment") {
    npmCommand.set(listOf("run", "webpack-development"))
}

tasks.register<NpmTask>("buildRelay") {
    npmCommand.set(listOf("run", "compile-relay"))
}

tasks.getByName<BootRun>("bootRun") {
    dependsOn("npm_install", "webpack")
}

node {
    version.set("16.18.0")
    npmVersion.set("8.19.2")
    download.set(true)
}

tasks.withType<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask> {
    schemaPaths = mutableListOf("${projectDir}/src/main/resources/schema")
    generateClient = true
    packageName = "com.kotlinspringgraphqlreact.graphql"
}