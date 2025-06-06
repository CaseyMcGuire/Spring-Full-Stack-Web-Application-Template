import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.gradle.node.npm.task.NpmTask
import org.springframework.boot.gradle.tasks.run.BootRun

val springVersion = "3.2.2"
val dgsVersion = "9.2.2"
// if you change this, you must update the `java.runtime.version` param in the 'system.properties' file to the same value
val javaVersion = "17"
val postgresVersion = "42.7.2"
val flywayVersion = "9.16.0"
val jooqVersion = "3.19.16"
val myNodeVersion = "20.11.0"
val myNpmVersion = "10.4.0"
val kotlinxHtmlVersion = "0.11.0"
val exposedVersion = "1.0.0-beta-2"

val pathToApplicationFolder = "src/main/kotlin/com"
val applicationFolder = File(rootProject.projectDir, pathToApplicationFolder)
val applicationFolderName = applicationFolder.list().singleOrNull()
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
  id("org.jetbrains.kotlin.jvm") version "1.9.22"
  // Kotlin makes all classes final by default but Spring relies
  // upon classes being extendable to implement certain functionality.
  // In my case, Spring Security's `@PreAuthorize` annotation wasn't working
  // but when I marked the class as `open`, dependency injection wouldn't work.
  // However, this plugin seems to fix both issues.
  // Read here for more info: https://kotlinlang.org/docs/all-open-plugin.html
  id("org.jetbrains.kotlin.plugin.spring") version "1.9.22"
  id("org.springframework.boot") version "3.2.2"
  id("io.spring.dependency-management") version "1.1.0"
  id("com.github.node-gradle.node") version "7.0.2"
  id("com.netflix.dgs.codegen") version "5.2.4"
  id("org.jooq.jooq-codegen-gradle") version "3.19.16"
  id("org.flywaydb.flyway") version "9.6.0"
  id("java")
}

dependencyManagement {
  imports {
    mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${dgsVersion}")
    mavenBom("org.springframework.boot:spring-boot-dependencies:${springVersion}")
  }
}

group = "com.application"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-security")

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
  implementation("org.jetbrains.exposed:exposed-migration:$exposedVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")

  // With these two dependencies, Spring will automatically run Flyway migrations on startup. See:
  // https://flywaydb.org/documentation/usage/plugins/springboot
  // https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties.data-migration
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.flywaydb:flyway-core:$flywayVersion")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = javaVersion
  }
}

tasks.withType<JavaCompile> {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
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
