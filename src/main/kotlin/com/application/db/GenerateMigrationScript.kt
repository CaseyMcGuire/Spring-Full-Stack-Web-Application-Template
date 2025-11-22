package com.application.db

import com.application.db.tables.PostsTable
import com.application.db.tables.UsersTable
import io.github.classgraph.ClassGraph
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

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

  val tableObjects = ClassGraph()
    .enableAllInfo()
    .acceptPackages("com.application.db.tables")
    .scan()
    .getSubclasses(Table::class.java.name)
    .loadClasses(Table::class.java)
    .mapNotNull { it.kotlin.objectInstance }
    .toTypedArray()

  val statements = transaction {
    MigrationUtils.statementsRequiredForDatabaseMigration(*tableObjects)
  }

  if (statements.isEmpty()) {
    println("Nothing to migrate.")
    return
  }

  println("Database migration statements: ")
  statements.forEach {
    println(it)
  }
  println("================================")
}

fun main(args: Array<String>) {
  generateSingleScript()
}