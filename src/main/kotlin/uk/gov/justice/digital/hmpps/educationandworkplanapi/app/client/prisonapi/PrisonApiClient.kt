package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonerInPrisonSummary

private val log = KotlinLogging.logger {}

@Component
class PrisonApiClient(
  @Qualifier("prisonApiWebClient")
  private val prisonApiWebClient: WebClient,
  private val prisonApiMapper: PrisonApiMapper,
) {

  /**
   * Retrieves a Prisoner's current and previous prisons, including the dates they entered/exited each prison.
   *
   *  @throws [PrisonApiException] if there is a problem retrieving the data.
   */
  fun getPrisonMovementEvents(prisonNumber: String): PrisonMovementEvents {
    val prisonerInPrisonSummary = prisonApiWebClient
      .get()
      .uri("/api/offenders/$prisonNumber/prison-timeline")
      .retrieve()
      .bodyToMono(PrisonerInPrisonSummary::class.java)
      .onErrorResume {
        Mono.error(PrisonApiException("Error retrieving prison history for Prisoner $prisonNumber", it))
      }
      .block()

    val numberOfPeriods = prisonerInPrisonSummary!!.prisonPeriod?.size ?: 0
    log.info { "Retrieved $numberOfPeriods prison periods from the prison-api for prisoner $prisonNumber" }
    return prisonApiMapper.toPrisonMovementEvents(prisonNumber, prisonerInPrisonSummary)
  }
}
