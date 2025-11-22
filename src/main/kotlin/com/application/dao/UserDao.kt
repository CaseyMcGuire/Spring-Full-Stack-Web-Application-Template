package com.application.dao


import com.application.db.tables.UsersTable
import com.application.services.User
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Component

@Component
class UserDao {

  fun findByEmail(email: String): User? {
    return transaction {
      val row = UsersTable
        .select(UsersTable.email, UsersTable.hashedPassword)
        .where {
          UsersTable.email eq email
        }
        .singleOrNull()
        ?: return@transaction null

      User(row[UsersTable.email], row[UsersTable.hashedPassword], null)
    }
  }

  fun createUser(email: String, hashedPassword: String): User {
    return transaction {
      val insertedEmail = UsersTable.insert {
        it[UsersTable.email] = email
        it[UsersTable.hashedPassword] = hashedPassword
      } get UsersTable.email

      findByEmail(insertedEmail) ?:
        throw RuntimeException("Failed to insert: $insertedEmail")
    }
  }
}