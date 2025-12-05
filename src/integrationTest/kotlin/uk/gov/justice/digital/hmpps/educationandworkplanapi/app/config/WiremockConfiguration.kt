package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

@Configuration
class WiremockConfiguration {

  companion object {
    private val listenerRegistered = AtomicBoolean(false)

    /** Shared WireMock instance for all Spring test contexts */
    val sharedWireMockServer: WireMockServer by lazy {
      WireMockServer(
        options()
          .port(9093)
          .usingFilesUnderClasspath("simulations"),
      ).apply { start() }
    }
  }

  @Bean
  fun wireMockServer(
    @Value("\${logWiremockRequests:false}") logWiremockRequests: Boolean,
  ): WireMockServer {
    val server = sharedWireMockServer

    if (logWiremockRequests && listenerRegistered.compareAndSet(false, true)) {
      server.addMockServiceRequestListener { request: Request, _: Response ->
        val headers = request.headers.all()
          .joinToString("\n") { "${it.key()}: ${it.values().joinToString(", ")}" }

        val msg = """
          |WireMock request received:
          |${request.method} ${request.absoluteUrl}
          |
          |$headers
          |
          |${request.bodyAsString}
        """.trimMargin()

        logger.info { msg }
      }
    }

    return server
  }
}
