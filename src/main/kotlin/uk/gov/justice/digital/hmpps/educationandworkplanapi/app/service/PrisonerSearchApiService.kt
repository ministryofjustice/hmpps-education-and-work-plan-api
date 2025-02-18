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

  fun getAllPrisonersInPrison(prisonId: String): List<Prisoner> {
    var page = 0
    val pageSize = 250

    val prisoners = mutableListOf<Prisoner>()

    do {
      val apiResponse = prisonerSearchApiClient.getPrisonersByPrisonId(
        prisonId = prisonId,
        page = page++,
        pageSize = pageSize,
      )
      prisoners.addAll(apiResponse.content)
    } while (apiResponse.last != true)

    return prisoners.toList()
      .also {
        log.debug { "Returned ${it.size} prisoners for prison $prisonId from $page calls to Prisoner Search API" }
      }
  }

  fun getPrisoner(prisonNumber: String): Prisoner = prisonerSearchApiClient.getPrisoner(prisonNumber)
    .also {
      log.debug { "Retrieved prisoner [$prisonNumber] from Prisoner Search API" }
    }
}
