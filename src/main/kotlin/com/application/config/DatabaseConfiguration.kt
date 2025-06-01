package com.application.config

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.spring.transaction.SpringTransactionManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DatabaseConfiguration(val dataSource: DataSource) {

  @Bean
  fun transactionManager(): SpringTransactionManager {
    return SpringTransactionManager(dataSource)
  }

  // transactionManager is added as a parameter but not used because Gemini said it would ensure that
  // it's initialized before the database is. I'm not really sure why that's necessary but I did it anyways
  @Bean
  fun database(dataSource: DataSource, transactionManager: SpringTransactionManager): Database {
    return Database.connect(dataSource)
  }

}