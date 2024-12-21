package com.kotlinspringgraphqlreact.services

import com.kotlinspringgraphqlreact.dao.UserDao
import com.kotlinspringgraphqlreact.db.models.UserDetailsImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val userDao: UserDao): UserDetailsService {

  @Throws(UsernameNotFoundException::class)
  override fun loadUserByUsername(username: String?): UserDetails {
    val exception = UsernameNotFoundException("User with given email not found")
    if (username == null) {
      throw exception
    }
    val user = userDao.findByUsername(username)
    if (user != null) {
      return UserDetailsImpl(user)
    }
    throw exception
  }

}

data class User(val username: String, val hashedPassword: String, val role: String?)