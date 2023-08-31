package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

@Configuration
class WiremockConfiguration {

  @Bean
  fun wireMockServer(
    applicationContext: ConfigurableApplicationContext,
    @Value("\${logWiremockRequests:false}") logWiremockRequests: Boolean,
  ): WireMockServer =
    WireMockServer(
      options()
        .dynamicPort(),
    ).apply {
      if (logWiremockRequests) {
        addMockServiceRequestListener { request: Request, _: Response ->
          val formattedHeaders = request.headers.all().joinToString("\n") {
            "${it.key()}: ${it.values().joinToString(", ")}"
          }
          val logMessage = StringBuilder()
            .appendLine("Request sent to wiremock:")
            .appendLine("${request.method} ${request.absoluteUrl}")
            .appendLine(formattedHeaders)
            .appendLine()
            .appendLine(request.bodyAsString)
          logger.info { logMessage }
        }
      }
      start()
      TestPropertyValues.of(
        "api.hmpps-auth.url=http://localhost:${this.port()}/auth",
      ).applyTo(applicationContext)
    }
}
