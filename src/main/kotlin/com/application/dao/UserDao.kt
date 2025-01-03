package com.application.dao

import com.application.services.User
import org.springframework.stereotype.Component

@Component
class UserDao {
  private val inMemoryUserDb = mutableMapOf<String, User>()

  fun findByUsername(username: String): User? {
    return inMemoryUserDb[username]
  }

  fun createUser(username: String, hashedPassword: String): User {
    val user = User(username, hashedPassword, null)
    inMemoryUserDb[username] = user
    return user
  }
}