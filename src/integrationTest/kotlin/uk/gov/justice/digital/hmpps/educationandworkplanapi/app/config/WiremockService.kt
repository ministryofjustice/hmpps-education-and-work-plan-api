package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.stereotype.Service

@Service
class WiremockService(private val wireMockServer: WireMockServer) {

  fun resetAllStubsAndMappings() {
    wireMockServer.resetAll()
  }

  fun stubHmppsAuthUserMe(
    username: String = "auser_gen",
    displayName: String = "Albert User",
    activeCaseLoadId: String = "BXI",
  ) {
    wireMockServer.stubFor(
      WireMock.get(WireMock.urlPathMatching("/auth/api/user/me"))
        .willReturn(
          ResponseDefinitionBuilder.responseDefinition()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
                {
                    "username": "$username",
                    "active": true,
                    "name": "$displayName",
                    "authSource": "nomis",
                    "staffId": 486572,
                    "activeCaseLoadId": "$activeCaseLoadId",
                    "userId": "486572",
                    "uuid": "9d125413-9222-4bc7-ad08-24c26d64bb90"
                }
              """.trimIndent(),
            ),
        ),
    )
  }
}
