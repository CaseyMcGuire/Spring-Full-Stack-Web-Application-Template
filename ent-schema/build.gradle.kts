plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(21)
}

repositories {
  mavenCentral()
  mavenLocal()
}

val entktVersion: String by project

dependencies {
  implementation("io.entkt:schema:$entktVersion")
}
