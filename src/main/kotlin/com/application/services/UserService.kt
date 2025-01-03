package com.application.services

import com.application.dao.UserDao
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
  private val userDao: UserDao,
  private val passwordEncoder: PasswordEncoder
) {

  fun getUserByUsername(email: String): User? {
    return userDao.findByUsername(email)
  }

  fun registerUser(username: String, password: String) {
    val existingUser = userDao.findByUsername(username)
    if (existingUser != null) {
      throw IllegalArgumentException("A user with that username already exists")
    }
    val hashedPassword = passwordEncoder.encode(password)
    userDao.createUser(username, hashedPassword)
  }
}