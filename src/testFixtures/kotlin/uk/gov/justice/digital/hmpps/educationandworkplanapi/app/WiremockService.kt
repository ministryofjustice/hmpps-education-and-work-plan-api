package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PagedPrisonerResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonerInPrisonSummary

/**
 * Service class to provide support to tests with setting up and managing wiremock stubs
 */
@Service
class WiremockService(private val wireMockServer: WireMockServer) {

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  fun resetAllStubsAndMappings() {
    wireMockServer.resetAll()
  }

  fun stubGetPrisonTimelineFromPrisonApi(prisonNumber: String, response: PrisonerInPrisonSummary) {
    wireMockServer.stubFor(
      get(urlPathMatching("/api/offenders/$prisonNumber/prison-timeline"))
        .willReturn(
          responseDefinition()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubGetPrisonTimelineNotFound(prisonNumber: String) {
    wireMockServer.stubFor(
      get(urlPathMatching("/api/offenders/$prisonNumber/prison-timeline"))
        .willReturn(
          responseDefinition()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }

  fun stubGetPrisonerFromPrisonerSearchApi(prisonNumber: String, response: Prisoner) {
    wireMockServer.stubFor(
      get(urlPathMatching("/prisoner/$prisonNumber"))
        .willReturn(
          responseDefinition()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(response)),
        ),
    )
  }

  fun stubPrisonersInAPrisonSearchApi(prisonId: String, response: List<Prisoner>) {
    wireMockServer.stubFor(
      get(urlPathMatching("/prisoner-search/prison/$prisonId"))
        .willReturn(
          responseDefinition()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(PagedPrisonerResponse(last = true, content = response))),
        ),
    )
  }

  fun stubGetPrisonerNotFound(prisonNumber: String) {
    wireMockServer.stubFor(
      get(urlPathMatching("/prisoner/$prisonNumber"))
        .willReturn(
          responseDefinition()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"),
        ),
    )
  }
}
