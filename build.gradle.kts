import com.github.gradle.node.npm.task.NpmTask
import org.springframework.boot.gradle.tasks.run.BootRun
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val springVersion = "4.0.6"
val dgsVersion = "12.0.0"
// if you change this, you must update the `java.runtime.version` param in the 'system.properties' file to the same value
val javaVersion = 21
val postgresVersion = "42.7.11"
val flywayVersion = "12.6.2" // Matched to the plugin version in target file
val jooqVersion = "3.21.4"
val myNodeVersion = "20.11.0"
val myNpmVersion = "10.4.0"
val kotlinxHtmlVersion = "0.12.0"
val exposedVersion = "1.3.0"
// Matches the version managed by the spring-boot-dependencies BOM. It has to be stated explicitly because
// that BOM provides Testcontainers through a nested testcontainers-bom import, which the
// io.spring.dependency-management plugin doesn't surface to the org.testcontainers:* coordinates.
val testcontainersVersion = "2.0.5"

val pathToApplicationFolder = "src/main/kotlin/com"
val applicationFolder = File(rootProject.projectDir, pathToApplicationFolder)
val applicationFolderName = applicationFolder.list()?.singleOrNull()
  ?: throw IllegalStateException(
    "This application assumes that the path to the application code is $pathToApplicationFolder " +
        "with a single folder. However, it instead found the following files: ${applicationFolder.list()}. This assumption is " +
        "needed because some libraries use codegen and we need to keep the paths we pass to these libraries to be consistent " +
        "with the project structure."
  )

val jooqCodegenPath = "src/main/kotlin/com/${applicationFolderName}/db/codegen"
val dgsCodegenPackage = "com.${applicationFolderName}.graphql"
val migrationScriptPath = "com.application.db.GenerateMigrationScriptKt"

plugins {
  id("org.jetbrains.kotlin.jvm") version "2.3.21"
  // Kotlin makes all classes final by default but Spring relies
  // upon classes being extendable to implement certain functionality.
  id("org.jetbrains.kotlin.plugin.spring") version "2.3.21"
  id("org.springframework.boot") version "4.0.6"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.github.node-gradle.node") version "7.1.0"
  id("com.netflix.dgs.codegen") version "8.5.0"
  id("org.jooq.jooq-codegen-gradle") version "3.21.4"
  id("org.flywaydb.flyway") version "12.6.2"
  id("java")
}

dependencyManagement {
  imports {
    mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${dgsVersion}")
    mavenBom("org.springframework.boot:spring-boot-dependencies:${springVersion}")
  }
  dependencies {
    // DGS 12 needs json-path 3.0.0 for its Jackson 3 integration (com.jayway.jsonpath.spi.json.Jackson3JsonProvider).
    // The Spring Boot 4 BOM is imported last and otherwise wins, forcing the older 2.10.0 (even over DGS's
    // strict 3.0.0 requirement); 2.10.0 lacks that class, so the DGS query executor fails to instantiate at
    // startup. Pin 3.0.0 explicitly for every configuration.
    dependency("com.jayway.jsonpath:json-path:3.0.0")
  }
}

group = "com.application"
version = "1.0-SNAPSHOT"

springBoot {
  mainClass.set("com.application.MainKt")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-security")

  implementation("com.netflix.graphql.dgs:dgs-starter")

  // for application runtime
  implementation("org.jooq:jooq:$jooqVersion")
  implementation("org.jooq:jooq-meta:$jooqVersion")
  implementation("org.jooq:jooq-codegen:$jooqVersion")

  // This ensures these libraries will be on the classpath for the jooqCodegen gradle task
  jooqCodegen("org.jooq:jooq-codegen:$jooqVersion")
  jooqCodegen("org.jooq:jooq-meta:$jooqVersion")
  jooqCodegen("org.postgresql:postgresql:$postgresVersion")
  jooqCodegen(project(":customgenerator"))

  implementation("org.postgresql:postgresql:${postgresVersion}")
  implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
  implementation("org.jetbrains.exposed:spring-transaction:$exposedVersion")

  implementation("org.jetbrains.exposed:exposed-migration-core:$exposedVersion")
  implementation("org.jetbrains.exposed:exposed-migration-jdbc:$exposedVersion")

  implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  // spring-boot-starter-jdbc supplies the DataSource/HikariCP autoconfiguration (consumed by
  // DatabaseConfiguration and by Flyway). We deliberately avoid spring-boot-starter-data-jpa: this app
  // does its data access with Exposed + jOOQ, so Hibernate/Spring Data JPA would be dead weight.
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  // Flyway applies its migrations on startup. Spring Boot 4 split each integration's autoconfiguration
  // into its own module, so the Flyway autoconfiguration (formerly bundled in spring-boot-autoconfigure)
  // must be pulled in explicitly via spring-boot-flyway, alongside the engine and the Postgres support.
  implementation("org.springframework.boot:spring-boot-flyway")
  implementation("org.flywaydb:flyway-core:$flywayVersion")
  implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

  implementation("io.github.classgraph:classgraph:4.8.184")

  // Testing. The spring-boot-* artifacts are versioned by the spring-boot-dependencies BOM; the
  // org.testcontainers:* modules are pinned to $testcontainersVersion (see the note by its declaration).
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter:$testcontainersVersion")
  testImplementation("org.testcontainers:testcontainers-postgresql:$testcontainersVersion")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(javaVersion))
  }
}

kotlin {
  jvmToolchain(javaVersion)
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_21)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(javaVersion)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

tasks.register<NpmTask>("webpack") {
  npmCommand.set(listOf("run", "webpack"))
}

tasks.register<NpmTask>("webpackDevelopment") {
  npmCommand.set(listOf("run", "webpack-development"))
}

tasks.register<NpmTask>("buildRelay") {
  npmCommand.set(listOf("run", "relay-compiler"))
}

// make sure webpack runs before the processResources task so the TypeScript files are compiled before
// being copied into the build folder
tasks.processResources {
  val taskNames = gradle.startParameter.taskNames
  // only run frontend tasks when we're doing a full build
  if (taskNames.any { it.contains("bootRun", ignoreCase = true) }) {
    dependsOn("npm_install", "webpack")
  }
}

// Note the node and npm variables can't be named `nodeVersion` and `npmVersion` since it interferes with
// the plugin
node {
  version.set(myNodeVersion)
  npmVersion.set(myNpmVersion)
  download.set(true)
}

tasks.withType<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask> {
  schemaPaths = mutableListOf("${projectDir}/src/main/resources/schema")
  generateClient = true
  packageName = dgsCodegenPackage
}

val getEnvironmentVariables = fun(): Map<String, String> {
  val map = hashMapOf<String, String>()

  val envFile = file(".env")
  if (!envFile.exists()) {
    return map
  }
  envFile.readLines().forEach {
    val (key, value) = it.split("=")
    map[key] = value
  }
  return map
}
val envVariables: Map<String, String> = getEnvironmentVariables()

val dbUser = envVariables.getValue("DB_USER")
val dbPassword = envVariables.getValue("DB_PASSWORD")
val dbUrl = envVariables.getValue("DB_URL_PREFIX") + envVariables.getValue("DB_NAME")

tasks.getByName<BootRun>("bootRun") {
  // This makes the environment variables specified in the .env file accessible to the application
  environment = envVariables
  mainClass.set("com.application.MainKt")
}

tasks.register<JavaExec>("generateMigrationScript") {
  description = "Generates a SQL migration script which can be used by Flyway."
  // This tells Gradle to use the project's compiled classes and all its dependencies
  classpath = sourceSets.main.get().runtimeClasspath
  environment = envVariables
  mainClass.set(migrationScriptPath)
}

// Spring automatically handles flyway migrations but adding this task allows running flyway tasks
// from the command line. See: https://flywaydb.org/documentation/usage/gradle/
flyway {
  url = dbUrl
  user = dbUser
  password = dbPassword
}

jooq {
  configuration {
    jdbc {
      driver = "org.postgresql.Driver"
      url = dbUrl
      user = dbUser
      password = dbPassword
    }
    generator {
      name = "org.jooq.codegen.KotlinGenerator"
      target {
        packageName = "generated.jooq"
        directory = jooqCodegenPath
      }
      database {
        // See https://www.postgresqltutorial.com/postgresql-administration/postgresql-schema/
        // for more explanation about the difference between databases and schemas in Postgres
        inputSchema = "public"
        excludes = "flyway_schema_history"
      }
      generate {
        isImmutablePojos = true
      }
      strategy {
        // Note: In order for this to work, this class must be in a different gradle project and the gradle project
        // must be included as a dependency of the jooqCodegen gradle task (see above in 'dependencies' block).
        // See https://github.com/etiennestuder/gradle-jooq-plugin/blob/ac7f25ada8c8a15b0e3692ef038f6dd0fd6a42ac/example/configure_custom_generator_strategy/build.gradle#L12
        name = "com.application.CustomGeneratorStrategy"
      }
    }
  }
}