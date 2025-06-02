package com.application.db.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object PostsTable : Table("posts") {
  val id = long("id").autoIncrement()
  val title = text("title")
  val content = text("content")
  val createdAt = datetime(name = "created_at")
  val updatedAt = datetime(name = "updated_at")
  val userId = long("user_id").references(
    UsersTable.id,
    onDelete = ReferenceOption.NO_ACTION,
    onUpdate = ReferenceOption.NO_ACTION,
    "posts_user_id_fkey")
  override val primaryKey = PrimaryKey(id)
}