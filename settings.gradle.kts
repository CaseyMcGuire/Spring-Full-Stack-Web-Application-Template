pluginManagement {
  repositories {
    // spa-routing's Gradle plugin is published to mavenLocal
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "application"

// Single source of truth for SPA route definitions (see AGENTS.md)
include("spa-route-definitions")

fun includeSubmodulesFromDirectory(directoryName: String) {
  val submodulesDir = file(directoryName)
  if (submodulesDir.exists() && submodulesDir.isDirectory) {
    submodulesDir.listFiles()?.forEach { dir ->
      if (dir.isDirectory && File(dir, "build.gradle.kts").exists()) {
        val moduleName = dir.name
        include(moduleName)
        project(":$moduleName").projectDir = dir
      }
    }
  } else {
    println("Directory '$directoryName' does not exist or is not a directory.")
  }
}

includeSubmodulesFromDirectory("submodules")