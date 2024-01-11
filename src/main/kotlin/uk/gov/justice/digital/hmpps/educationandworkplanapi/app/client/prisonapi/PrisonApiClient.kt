package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonerInPrisonSummary

@Component
class PrisonApiClient(private val prisonApiWebClient: WebClient) {

  /**
   * Retrieves a Prisoner's current and previous prisons, including the dates they entered/exited each prison.
   *
   *  @throws [PrisonApiException] if there is a problem retrieving the data.
   */
  fun getPrisonMovementEvents(prisonNumber: String): PrisonMovementEvents? {
    prisonApiWebClient
      .get()
      .uri("/api/offenders/$prisonNumber/prison-timeline")
      .retrieve()
      .bodyToMono(PrisonerInPrisonSummary::class.java)
      // TODO RR-566 - hande PrisonApiException (e.g. without returning an error to the UI)
      .onErrorResume {
        Mono.error(PrisonApiException("Error retrieving prison history for Prisoner $prisonNumber", it))
      }
      .block()
    // TODO RR-580 - map PrisonerInPrisonSummary to PrisonMovementEvents
    return PrisonMovementEvents(prisonNumber, emptyMap())
  }
}
