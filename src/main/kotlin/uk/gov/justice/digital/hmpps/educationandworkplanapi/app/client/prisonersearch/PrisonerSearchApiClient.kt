package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
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
}
