package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model.CiagInductionResponse

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

  fun stubGetInductionFromCiagApi(ciagResponse: CiagInductionResponse) {
    val prisonNumber = ciagResponse.offenderId
    wireMockServer.stubFor(
      get(urlPathMatching("/ciag/induction/$prisonNumber"))
        .willReturn(
          responseDefinition()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(ciagResponse)),
        ),
    )
  }
}
