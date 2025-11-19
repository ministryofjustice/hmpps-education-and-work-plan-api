package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties for external API integrations.
 *
 * Properties are bound from the `api` and `apis` prefixes in application configuration.
 */
@ConfigurationProperties(prefix = "api")
data class ApiProperties(
  /**
   * Global timeout for API calls.
   * Defaults to 20 seconds if not specified.
   */
  val timeout: Duration = Duration.ofSeconds(20),
)

/**
 * Configuration properties for external API URLs.
 *
 * Properties are bound from the `apis` prefix in application configuration.
 */
@ConfigurationProperties(prefix = "apis")
data class ApisProperties(
  /**
   * Prison API configuration.
   */
  @field:Valid
  val prisonApi: ApiEndpoint,

  /**
   * Manage Users API configuration.
   */
  @field:Valid
  val manageUsersApi: ApiEndpoint,

  /**
   * Prisoner Search API configuration.
   */
  @field:Valid
  val prisonerSearchApi: ApiEndpoint,
) {
  /**
   * Configuration for an individual API endpoint.
   */
  data class ApiEndpoint(
    /**
     * Base URL for the API endpoint.
     */
    @field:NotBlank(message = "API URL must not be blank")
    val url: String,
  )
}
