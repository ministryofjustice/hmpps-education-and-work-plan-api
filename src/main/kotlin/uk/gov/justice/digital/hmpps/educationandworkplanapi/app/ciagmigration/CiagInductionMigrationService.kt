package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.CiagInductionResponse

private val log = KotlinLogging.logger {}

/**
 * Service class to orchestrate calls to CIAG Induction API in order to migrate CIAG Inductions from the CIAG API to this
 * API and database.
 */
@Service
class CiagInductionMigrationService(private val ciagWebClient: WebClient) {

  fun migrateCiagInductions() {
    log.info { "Starting migration of CIAG Inductions from CIAG API to PLP API" }

    try {
      val ciagInduction = ciagWebClient
        .get()
        .uri("/ciag/induction/A5077DY")
        .retrieve()
        .bodyToMono(CiagInductionResponse::class.java)
        .block()

      log.info { "Deserialised CIAG Induction: $ciagInduction" }
    } catch (e: Exception) {
      log.error("Error calling CIAG API to get CIAG Induction", e)
    }
  }
}
