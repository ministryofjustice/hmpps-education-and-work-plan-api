package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class PrisonerSearchApiClient(
  @Qualifier("prisonerSearchApiWebClient")
  private val prisonerSearchApiWebClient: WebClient,
) {

  companion object {
    const val FIRST_PAGE = 0
    const val DEFAULT_PAGE_SIZE = 9999
  }

  /**
   * Returns a paged list of prisoners in a given prison identified by its prison ID.
   *
   * The page number and page size can be controlled with the relevant method args; otherwise the first page using a page
   * size of 9999 is returned; which for all intents and purposes would be all prisoners in the prison.
   */
  fun getPrisonersByPrisonId(prisonId: String, page: Int = FIRST_PAGE, pageSize: Int = DEFAULT_PAGE_SIZE): PagedPrisonerResponse =
    prisonerSearchApiWebClient
      .get()
      .uri {
        it.path("/prisoner-search/prison/$prisonId")
          .queryParam("page", page)
          .queryParam("size", pageSize)
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
  fun getPrisoner(prisonNumber: String): Prisoner =
    try {
      prisonerSearchApiWebClient
        .get()
        .uri("/prisoner/{prisonNumber}", prisonNumber)
        .headers {
          it.contentType = MediaType.APPLICATION_JSON
        }
        .retrieve()
        .bodyToMono(Prisoner::class.java)
        .block()!!
    } catch (e: WebClientResponseException.NotFound) {
      throw PrisonerNotFoundException(prisonNumber)
    } catch (e: Exception) {
      throw PrisonerSearchApiException("Error retrieving prisoner by prisonNumber $prisonNumber", e)
    }
}
