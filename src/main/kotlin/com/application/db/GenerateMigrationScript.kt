package com.application.db

import com.application.db.tables.Users
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun generateSingleScript() {
  val databaseUrlPrefix = System.getenv("DB_URL_PREFIX") ?:
    throw Exception("DB_URL_PREFIX is not set")
  val databaseName = System.getenv("DB_NAME") ?:
    throw Exception("DB_NAME is not set")
  val databaseUser = System.getenv("DB_USER") ?:
    throw Exception("DB_USER is not set")
  val databasePassword = System.getenv("DB_PASSWORD") ?:
    throw Exception("DB_PASSWORD is not set")
  val databaseUrl = databaseUrlPrefix + databaseName

  Database.connect(
    url = databaseUrl,
    driver = "org.postgresql.Driver",
    user = databaseUser,
    password = databasePassword
  )

  // scriptDirectory = "src/main/resources/db/migration",
  val statements = transaction {
    MigrationUtils.statementsRequiredForDatabaseMigration(
      Users
    )
  }

  println("Database migration statements: ")
  statements.forEach {
    println(it)
  }
}

fun main(args: Array<String>) {
  generateSingleScript()
}