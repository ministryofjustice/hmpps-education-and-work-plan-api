package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.ServerSocket

object PostgresContainer {
  val instance: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }
  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? = if (checkPostgresRunning().not()) {
    PostgreSQLContainer<Nothing>("postgres:15.5").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withExposedPorts(5432)
      withDatabaseName("education_and_work_plan_api_db")
      withUsername("admin")
      withPassword("admin_password")
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)
      start()
    }
  } else {
    null
  }

  private fun checkPostgresRunning(): Boolean = try {
    val serverSocket = ServerSocket(5432)
    serverSocket.localPort == 0
  } catch (e: IOException) {
    true
  }
}
