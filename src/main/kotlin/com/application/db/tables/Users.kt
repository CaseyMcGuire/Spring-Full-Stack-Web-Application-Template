package com.application.db.tables

import org.jetbrains.exposed.v1.core.Table

object Users : Table("users") {
  val id = long("id").autoIncrement()
  val email = varchar("email", 255).uniqueIndex()
  val hashedPassword = varchar("hashed_password", 255)

  override val primaryKey = PrimaryKey(id)
}