package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.applicationinsights.TelemetryClient
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithEditAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithViewAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.TimelineRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.InboundEventsService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.LocalStackContainer
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.security.KeyPair
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient(timeout = "PT5M")
abstract class IntegrationTestBase {

  companion object {
    const val CREATE_GOALS_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
    const val CREATE_ACTION_PLAN_URI_TEMPLATE = "/action-plans/{prisonNumber}"
    const val GET_ACTION_PLAN_URI_TEMPLATE = "/action-plans/{prisonNumber}"
    const val GET_TIMELINE_URI_TEMPLATE = "/timelines/{prisonNumber}"
    const val CIAG_INDUCTION_URI_TEMPLATE = "/ciag/induction/{prisonNumber}"
    const val INDUCTION_URI_TEMPLATE = "/inductions/{prisonNumber}"

    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
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
  lateinit var wiremockService: WiremockService

  @Autowired
  lateinit var actionPlanRepository: ActionPlanRepository

  @Autowired
  lateinit var timelineRepository: TimelineRepository

  @Autowired
  lateinit var inductionRepository: InductionRepository

  @SpyBean
  lateinit var telemetryClient: TelemetryClient

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var keyPair: KeyPair

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @SpyBean
  protected lateinit var inboundEventsServiceSpy: InboundEventsService

  private val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId("domainevents") ?: throw MissingQueueException("HmppsTopic domainevents not found")
  }

  protected val snsClient by lazy { domainEventsTopic.snsClient }
  protected val domainEventsTopicArn by lazy { domainEventsTopic.arn }

  @BeforeEach
  fun clearDatabase() {
    actionPlanRepository.deleteAll() // Will also remove all Goals and Steps due to cascade
    timelineRepository.deleteAll() // Will also remove all TimelineEvents due to cascade
    inductionRepository.deleteAll()
  }

  @BeforeEach
  fun resetWiremock() {
    wiremockService.resetAllStubsAndMappings()
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

  fun createCiagInduction(
    prisonNumber: String,
    createCiagInductionRequest: CreateCiagInductionRequest,
    username: String = "auser_gen",
    displayName: String = "Albert User",
  ) {
    webTestClient.post()
      .uri(CIAG_INDUCTION_URI_TEMPLATE, prisonNumber)
      .withBody(createCiagInductionRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = username,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  fun createInduction(
    prisonNumber: String,
    createInductionRequest: CreateInductionRequest,
    username: String = "auser_gen",
    displayName: String = "Albert User",
  ) {
    webTestClient.post()
      .uri(INDUCTION_URI_TEMPLATE, prisonNumber)
      .withBody(createInductionRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = username,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  fun getCiagInduction(prisonNumber: String): CiagInductionResponse =
    webTestClient.get()
      .uri(CIAG_INDUCTION_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(CiagInductionResponse::class.java)
      .responseBody.blockFirst()!!

  fun getInduction(prisonNumber: String): InductionResponse =
    webTestClient.get()
      .uri(INDUCTION_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithViewAuthority(privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(InductionResponse::class.java)
      .responseBody.blockFirst()!!

  fun createGoal(
    prisonNumber: String,
    createGoalRequest: CreateGoalRequest,
    username: String = "auser_gen",
    displayName: String = "Albert User",
  ) {
    val createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(createGoalRequest))
    webTestClient.post()
      .uri("/action-plans/{prisonNumber}/goals", prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(
        aValidTokenWithEditAuthority(
          username = username,
          displayName = displayName,
          privateKey = keyPair.private,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }
}
