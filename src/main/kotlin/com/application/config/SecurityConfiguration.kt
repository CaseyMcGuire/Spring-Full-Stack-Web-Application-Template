package com.application.config

import com.application.services.UserDetailsServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler

@EnableWebSecurity
@Configuration
open class SecurityConfiguration(private val userDetailsService: UserDetailsServiceImpl){

  @Bean
  open fun passwordEncoder(): PasswordEncoder? {
    return BCryptPasswordEncoder()
  }

  @Bean
  open fun authenticationManager(http: HttpSecurity): AuthenticationManager {
    val authenticationManagerBuilder = http.getSharedObject(
      AuthenticationManagerBuilder::class.java
    )
    authenticationManagerBuilder.userDetailsService(userDetailsService)
      .passwordEncoder(passwordEncoder())
    return authenticationManagerBuilder.build()
  }

  @Bean
  open fun filterChain(http: HttpSecurity): SecurityFilterChain {

    // Use the plain CsrfTokenRequestAttributeHandler (not the default XOR-based one)
    // to produce a stable CSRF token compatible with the SPA frontend.
    val requestHandler = CsrfTokenRequestAttributeHandler()
    http {
      authorizeHttpRequests {
        authorize("/**", permitAll)
      }
      formLogin {
        loginPage = "/login"
        defaultSuccessUrl("/", true)
        failureUrl = "/login?error=true"
        // note usernameParameter and password parameter aren't available yet but they will be at some point:
        // https://github.com/spring-projects/spring-security/issues/14474
      }
      csrf {
        csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse()
        csrfTokenRequestHandler = requestHandler
      }
      logout {
        logoutSuccessUrl = "/"
      }
    }

    return http.build();
  }
}