package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.applicationinsights.TelemetryClient
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
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
