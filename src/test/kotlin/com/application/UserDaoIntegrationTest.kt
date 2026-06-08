package com.application

import com.application.dao.UserDao
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Basic integration test. Boots the full Spring context against a throwaway PostgreSQL container,
 * lets Flyway apply the migrations in src/main/resources/db/migration, and exercises a real UserDao
 * round-trip. This covers the whole data path end to end: Testcontainers -> DataSource autoconfiguration
 * -> Flyway -> Exposed.
 *
 * @ServiceConnection points spring.datasource.* at the container automatically, so no manual property
 * wiring is needed. Requires a running Docker daemon.
 */
@Testcontainers
@SpringBootTest
class UserDaoIntegrationTest {

  @Autowired
  lateinit var userDao: UserDao

  @Test
  fun `persists a user and reads it back`() {
    val email = "integration@example.com"
    assertNull(userDao.findByEmail(email), "user should not exist before it is created")

    val created = userDao.createUser(email, "hashed-password")
    assertEquals(email, created.username)
    assertEquals("hashed-password", created.hashedPassword)

    val found = userDao.findByEmail(email)
    assertNotNull(found)
    assertEquals(email, found!!.username)
    assertEquals("hashed-password", found.hashedPassword)
  }

  companion object {
    @Container
    @ServiceConnection
    @JvmStatic
    val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
  }
}