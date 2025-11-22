package com.application.services

import com.application.dao.UserDao
import com.application.db.models.UserDetailsImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val userDao: UserDao): UserDetailsService {

  @Throws(UsernameNotFoundException::class)
  override fun loadUserByUsername(email: String?): UserDetails {
    val exception = UsernameNotFoundException("User with given email not found")
    if (email == null) {
      throw exception
    }
    val user = userDao.findByEmail(email)
    if (user != null) {
      return UserDetailsImpl(user)
    }
    throw exception
  }

}

data class User(val username: String, val hashedPassword: String, val role: String?)