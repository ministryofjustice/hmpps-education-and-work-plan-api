package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerSearchApiClient

private val log = KotlinLogging.logger {}

@Service
class PrisonerSearchApiService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {

  fun getAllPrisonersInPrison(prisonId: String): List<Prisoner> =
    prisonerSearchApiClient.getPrisonersByPrisonId(
      prisonId = prisonId,
      page = PrisonerSearchApiClient.FIRST_PAGE,
      pageSize = PrisonerSearchApiClient.DEFAULT_PAGE_SIZE,
    ).content
      .also {
        log.debug { "Returned ${it.size} prisoners for prison $prisonId from Prisoner Search API" }
      }

  fun getPrisoner(prisonNumber: String): Prisoner =
    prisonerSearchApiClient.getPrisoner(prisonNumber)
      .also {
        log.debug { "Retrieved prisoner [$prisonNumber] from Prisoner Search API" }
      }
}
