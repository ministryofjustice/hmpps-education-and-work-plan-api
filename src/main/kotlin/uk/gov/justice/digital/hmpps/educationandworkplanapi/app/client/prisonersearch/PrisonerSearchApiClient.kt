package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

@Component
class PrisonerSearchApiClient(
  @param:Qualifier("prisonerSearchApiWebClient")
  private val prisonerSearchApiWebClient: WebClient,
) {
  companion object {
    const val FIRST_PAGE = 0
    const val DEFAULT_PAGE_SIZE = 9999
    val RESPONSE_FIELDS = listOf(
      "prisonerNumber",
      "legalStatus",
      "releaseDate",
      "receptionDate",
      "prisonId",
      "indeterminateSentence",
      "recall",
      "lastName",
      "firstName",
      "dateOfBirth",
      "cellLocation",
      "nonDtoReleaseDateType",
    ).toTypedArray()

    private val log = KotlinLogging.logger {}
  }

  /**
   * Returns a paged list of prisoners in a given prison identified by its prison ID.
   *
   * The page number and page size can be controlled with the relevant method args; otherwise the first page using a page
   * size of 9999 is returned; which for all intents and purposes would be all prisoners in the prison.
   */
  fun getPrisonersByPrisonId(
    prisonId: String,
    page: Int = FIRST_PAGE,
    pageSize: Int = DEFAULT_PAGE_SIZE,
  ): PagedPrisonerResponse = prisonerSearchApiWebClient
    .get()
    .uri {
      it.path("/prisoner-search/prison/$prisonId")
        .queryParam("page", page)
        .queryParam("size", pageSize)
        .queryParam("responseFields", RESPONSE_FIELDS)
        .build()
    }
    .headers {
      it.contentType = MediaType.APPLICATION_JSON
    }
    .retrieve()
    .bodyToMono(PagedPrisonerResponse::class.java)
    .onErrorResume {
      Mono.error(PrisonerSearchApiException("Error retrieving prisoners by prisonId $prisonId", it))
    }
    .block()!!

  /**
   * Returns a specific prisoner from prisoner-search-api, identified by their prisonNumber
   */
  fun getPrisoner(prisonNumber: String): Prisoner = try {
    prisonerSearchApiWebClient
      .get()
      .uri("/prisoner/{prisonNumber}", prisonNumber)
      .headers {
        it.contentType = MediaType.APPLICATION_JSON
      }
      .retrieve()
      .bodyToMono(Prisoner::class.java)
      .retryWhen(
        Retry.backoff(3, Duration.ofMillis(500))
          .filter { ex -> ex is WebClientRequestException }
          .doBeforeRetry { retrySignal ->
            log.warn(
              "Retrying request for prisoner with prisonNumber:{} due to {} (attempt #{})",
              prisonNumber,
              retrySignal.failure().javaClass.simpleName,
              retrySignal.totalRetries() + 1,
            )
          },
      )
      .block()!!
  } catch (_: WebClientResponseException.NotFound) {
    throw PrisonerNotFoundException(prisonNumber)
  } catch (e: Exception) {
    throw PrisonerSearchApiException("Error retrieving prisoner by prisonNumber $prisonNumber", e)
  }
}
