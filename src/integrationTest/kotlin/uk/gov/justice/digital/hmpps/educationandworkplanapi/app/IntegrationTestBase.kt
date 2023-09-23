package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import com.microsoft.applicationinsights.TelemetryClient
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.TimelineRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.security.KeyPair
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integration-test")
abstract class IntegrationTestBase {

  companion object {
    const val CREATE_GOALS_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
    const val CREATE_ACTION_PLAN_URI_TEMPLATE = "/action-plans/{prisonNumber}"
    const val GET_ACTION_PLAN_URI_TEMPLATE = "/action-plans/{prisonNumber}"
    const val GET_TIMELINE_URI_TEMPLATE = "/timelines/{prisonNumber}"
  }

  init {
    // set awaitility defaults
    Awaitility.setDefaultPollInterval(500, MILLISECONDS)
    Awaitility.setDefaultTimeout(3, SECONDS)
  }

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var actionPlanRepository: ActionPlanRepository

  @Autowired
  lateinit var timelineRepository: TimelineRepository

  @SpyBean
  lateinit var telemetryClient: TelemetryClient

  @Autowired
  lateinit var keyPair: KeyPair

  @BeforeEach
  fun clearDatabase() {
    actionPlanRepository.deleteAll() // Will also remove all Goals and Steps due to cascade
    timelineRepository.deleteAll() // Will also remove all TimelineEvents due to cascade
  }

  fun getActionPlan(prisonNumber: String): ActionPlanResponse =
    webTestClient.get()
      .uri(GET_ACTION_PLAN_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .exchange()
      .returnResult(ActionPlanResponse::class.java)
      .responseBody.blockFirst()!!

  fun createActionPlan(
    prisonNumber: String,
    createActionPlanRequest: CreateActionPlanRequest = aValidCreateActionPlanRequest(),
  ) {
    webTestClient.post()
      .uri(CREATE_ACTION_PLAN_URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(aValidTokenWithEditAuthority(privateKey = keyPair.private))
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
  }

  fun getTimeline(prisonNumber: String): TimelineResponse =
    webTestClient.get()
      .uri(GET_TIMELINE_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .exchange()
      .returnResult(TimelineResponse::class.java)
      .responseBody.blockFirst()!!
}
