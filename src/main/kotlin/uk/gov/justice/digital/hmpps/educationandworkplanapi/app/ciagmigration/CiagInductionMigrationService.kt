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
class CiagInductionMigrationService(
  private val timelineLookupService: PrisonerInductionTimelineLookupService,
  private val ciagWebClient: WebClient,
) {

  fun migrateCiagInductions() {
    log.info { "Starting migration of CIAG Inductions from CIAG API to PLP API" }

    try {
      val prisonNumbers = timelineLookupService.getPrisonNumbersToMigrate()
      log.info { "Identified ${prisonNumbers.size} Inductions to migrate" }

      prisonNumbers.forEach { prisonNumber ->
        log.info { "Deserializing Induction for prisoner $prisonNumber" }
        val ciagInduction = getCiagInduction(prisonNumber)
      }

      log.info { "Finished migrating Inductions" }
    } catch (e: Exception) {
      log.error("Error migrating CIAG Inductions", e)
    }
  }

  private fun getCiagInduction(prisonNumber: String): CiagInductionResponse? =
    ciagWebClient
      .get()
      .uri("/ciag/induction/$prisonNumber")
      .retrieve()
      .bodyToMono(CiagInductionResponse::class.java)
      .block()
}
