import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.gradle.node.npm.task.NpmTask
import org.springframework.boot.gradle.tasks.run.BootRun

val springVersion = "3.2.2"
val dgsVersion = "9.2.2"
// if you change this, you must update the `java.runtime.version` param in the 'system.properties' file to the same value
val javaVersion = "17"
val postgresVersion = "42.7.2"
val flywayVersion = "9.16.0"
val jooqVersion = "3.14.1"
val myNodeVersion = "20.11.0"
val myNpmVersion = "10.4.0"
val kotlinxHtmlVersion = "0.11.0"

val pathToApplicationFolder = "src/main/kotlin/com"
val applicationFolder = File(rootProject.projectDir, pathToApplicationFolder)
val applicationFolderName = applicationFolder.list().singleOrNull()
  ?: throw IllegalStateException("This application assumes that the path to the application code is $pathToApplicationFolder " +
      "with a single folder. However, it instead found the following files: ${applicationFolder.list()}. This assumption is " +
      "needed because some libraries use codegen and we need to keep the paths we pass to these libraries to be consistent " +
      "with the project structure.")

val jooqCodegenPath = "src/main/kotlin/com/${applicationFolderName}/db/codegen"
val dgsCodegenPackage = "com.${applicationFolderName}.graphql"

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
  id("nu.studer.jooq") version "8.0"
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
  implementation("org.postgresql:postgresql:${postgresVersion}")
  jooqGenerator("org.postgresql:postgresql:${postgresVersion}")
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
  dependsOn("npm_install", "webpack")
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
val dbUrl = envVariables.getValue("DB_URL")

tasks.getByName<BootRun>("bootRun") {
  // This makes the environment variables specified in the .env file accessible to the application
  environment = envVariables
}

// Spring automatically handles flyway migrations but adding this task allows running flyway tasks
// from the command line. See: https://flywaydb.org/documentation/usage/gradle/
flyway {
  url = dbUrl
  user = dbUser
  password = dbPassword
}

jooq {
  version.set(jooqVersion)
  configurations {
    create("main") {
      generateSchemaSourceOnCompilation.set(false)
      jooqConfiguration.apply {
        jdbc.apply {
          driver = "org.postgresql.Driver"
          url = dbUrl
          user = dbUser
          password = dbPassword
        }
        generator.apply {
          name = "org.jooq.codegen.KotlinGenerator"
          target.apply {
            packageName = "generated.jooq"
            directory = jooqCodegenPath
          }
          database.apply {
            // See https://www.postgresqltutorial.com/postgresql-administration/postgresql-schema/
            // for more explanation about the difference between databases and schemas in Postgres
            inputSchema = "public"
            excludes = "flyway_schema_history"
          }
          generate.apply {
            isImmutablePojos = true
          }
        }
      }
    }

  }
}