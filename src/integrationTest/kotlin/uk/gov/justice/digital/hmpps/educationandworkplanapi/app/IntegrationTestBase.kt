package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.applicationinsights.TelemetryClient
import org.awaitility.Awaitility
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
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
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidPrisonerInPrisonSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ConversationRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.NoteRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PreviousQualificationsRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleHistoryRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.TimelineRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.ACTIONPLANS_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.ACTIONPLANS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.CONVERSATIONS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.EDUCATION_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.EDUCATION_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.GOALS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.INDUCTIONS_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.INDUCTIONS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.REVIEWS_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.TIMELINE_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.LocalStackContainer
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.PostgresContainer
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation.aValidCreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidCreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.security.KeyPair
import java.time.LocalDate
import java.util.UUID
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
    const val GET_ACTION_PLAN_REVIEWS_URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews"
    const val GET_TIMELINE_URI_TEMPLATE = "/timelines/{prisonNumber}"
    const val INDUCTION_URI_TEMPLATE = "/inductions/{prisonNumber}"
    const val EDUCATION_URI_TEMPLATE = "/person/{prisonNumber}/education"

    private val postgresContainer = PostgresContainer.instance

    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun configureTestContainers(registry: DynamicPropertyRegistry) {
      postgresContainer?.run {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
        registry.add("spring.datasource.username", postgresContainer::getUsername)
        registry.add("spring.datasource.password", postgresContainer::getPassword)
        registry.add("spring.datasource.placeholders.database_update_password", postgresContainer::getPassword)
        registry.add("spring.datasource.placeholders.database_read_only_password", postgresContainer::getPassword)
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl)
        registry.add("spring.flyway.user", postgresContainer::getUsername)
        registry.add("spring.flyway.password", postgresContainer::getPassword)
      }
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }

  init {
    // set awaitility defaults
    Awaitility.setDefaultPollInterval(500, MILLISECONDS)
    Awaitility.setDefaultTimeout(30, SECONDS)
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

  @Autowired
  lateinit var previousQualificationsRepository: PreviousQualificationsRepository

  @Autowired
  lateinit var conversationRepository: ConversationRepository

  @Autowired
  lateinit var reviewRepository: ReviewRepository

  @Autowired
  lateinit var reviewScheduleRepository: ReviewScheduleRepository

  @Autowired
  lateinit var reviewScheduleHistoryRepository: ReviewScheduleHistoryRepository

  @SpyBean
  lateinit var telemetryClient: TelemetryClient

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var keyPair: KeyPair

  @Autowired
  lateinit var noteRepository: NoteRepository

  @Autowired
  lateinit var inductionScheduleRepository: InductionScheduleRepository

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  val domainEventQueue by lazy {
    hmppsQueueService.findByQueueId("educationandworkplan")
      ?: throw MissingQueueException("HmppsQueue educationandworkplan not found")
  }
  val domainEventQueueClient by lazy { domainEventQueue.sqsClient }
  val domainEventQueueDlqClient by lazy { domainEventQueue.sqsDlqClient }

  val inductionScheduleEventQueue by lazy {
    hmppsQueueService.findByQueueId("inductionscheduleeventqueue")
      ?: throw MissingQueueException("HmppsQueue inductionscheduleeventqueue not found")
  }
  val testInductionScheduleEventQueueClient by lazy { inductionScheduleEventQueue.sqsClient }
  val testInductionScheduleEventQueueDlqClient by lazy { inductionScheduleEventQueue.sqsDlqClient }

  val reviewScheduleEventQueue by lazy {
    hmppsQueueService.findByQueueId("reviewscheduleeventqueue")
      ?: throw MissingQueueException("HmppsQueue reviewscheduleeventqueue not found")
  }
  val testReviewScheduleEventQueueClient by lazy { reviewScheduleEventQueue.sqsClient }
  val testReviewScheduleEventQueueDlqClient by lazy { reviewScheduleEventQueue.sqsDlqClient }

  @BeforeEach
  fun clearDatabase() {
    actionPlanRepository.deleteAll() // Will also remove all Goals and Steps due to cascade
    timelineRepository.deleteAll() // Will also remove all TimelineEvents due to cascade
    inductionRepository.deleteAll()
    previousQualificationsRepository.deleteAll()
    conversationRepository.deleteAll()
    reviewRepository.deleteAll()
    reviewScheduleRepository.deleteAll()

    // clear all the queues just in case there are any messages hanging around
    domainEventQueueClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(domainEventQueue.queueUrl).build()).get()
    domainEventQueueDlqClient!!.purgeQueue(PurgeQueueRequest.builder().queueUrl(domainEventQueue.dlqUrl).build())
      .get()

    testInductionScheduleEventQueueClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(inductionScheduleEventQueue.queueUrl).build(),
    ).get()
    testInductionScheduleEventQueueDlqClient!!.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(inductionScheduleEventQueue.dlqUrl).build(),
    ).get()
    testReviewScheduleEventQueueClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(reviewScheduleEventQueue.queueUrl).build(),
    ).get()
    testReviewScheduleEventQueueDlqClient!!.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(reviewScheduleEventQueue.dlqUrl).build(),
    ).get()
  }

  @BeforeEach
  fun resetWiremock() {
    wiremockService.resetAllStubsAndMappings()
  }

  fun getActionPlan(prisonNumber: String): ActionPlanResponse =
    webTestClient.get()
      .uri(GET_ACTION_PLAN_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
      .exchange()
      .returnResult(ActionPlanResponse::class.java)
      .responseBody.blockFirst()!!

  fun createActionPlan(
    prisonNumber: String,
    createActionPlanRequest: CreateActionPlanRequest = aValidCreateActionPlanRequest(),
    username: String = "auser_gen",
    displayName: String = "Albert User",
  ) {
    webTestClient.post()
      .uri(CREATE_ACTION_PLAN_URI_TEMPLATE, prisonNumber)
      .withBody(createActionPlanRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          ACTIONPLANS_RW,
          privateKey = keyPair.private,
          username = username,
          displayName = displayName,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
  }

  fun getTimeline(prisonNumber: String): TimelineResponse =
    run {
      wiremockService.stubGetPrisonTimelineFromPrisonApi(
        prisonNumber,
        aValidPrisonerInPrisonSummary(prisonerNumber = prisonNumber, prisonPeriod = emptyList()),
      )
      webTestClient.get()
        .uri(GET_TIMELINE_URI_TEMPLATE, prisonNumber)
        .bearerToken(aValidTokenWithAuthority(TIMELINE_RO, privateKey = keyPair.private))
        .exchange()
        .returnResult(TimelineResponse::class.java)
        .responseBody.blockFirst()!!
    }

  fun createPrisonerAPIStub(prisonNumber: String, prisoner: Prisoner): Prisoner =
    run {
      wiremockService.stubGetPrisonerFromPrisonerSearchApi(
        prisonNumber,
        prisoner,
      )
      return prisoner
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
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          privateKey = keyPair.private,
          username = username,
          displayName = displayName,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  fun getInduction(prisonNumber: String): InductionResponse =
    webTestClient.get()
      .uri(INDUCTION_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RO, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(InductionResponse::class.java)
      .responseBody.blockFirst()!!

  fun archiveGoal(
    prisonNumber: String,
    archiveGoalRequest: ArchiveGoalRequest,
    username: String = "auser_gen",
    displayName: String = "Albert User",
  ) {
    webTestClient.put()
      .uri("/action-plans/{prisonNumber}/goals/{goalReference}/archive", prisonNumber, archiveGoalRequest.goalReference)
      .withBody(archiveGoalRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
          username = username,
          displayName = displayName,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent
  }

  fun createConversation(
    prisonNumber: String,
    createConversationRequest: CreateConversationRequest = aValidCreateConversationRequest(),
    username: String = "auser_gen",
    displayName: String = "Albert User",
  ) {
    webTestClient.post()
      .uri("/conversations/{prisonNumber}", prisonNumber)
      .withBody(createConversationRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          CONVERSATIONS_RW,
          privateKey = keyPair.private,
          username = username,
          displayName = displayName,
        ),
      )
      .contentType(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  fun getEducation(prisonNumber: String): EducationResponse =
    webTestClient.get()
      .uri(EDUCATION_URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(EducationResponse::class.java)
      .responseBody.blockFirst()!!

  fun createEducation(
    prisonNumber: String,
    createEducationRequest: CreateEducationRequest = aValidCreateEducationRequest(),
    username: String = "auser_gen",
    displayName: String = "Albert User",
  ) {
    webTestClient.post()
      .uri(EDUCATION_URI_TEMPLATE, prisonNumber)
      .withBody(createEducationRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          EDUCATION_RW,
          username = username,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isCreated
  }

  fun getActionPlanReviews(prisonNumber: String): ActionPlanReviewsResponse =
    webTestClient.get()
      .uri(GET_ACTION_PLAN_REVIEWS_URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RO, privateKey = keyPair.private))
      .exchange()
      .returnResult(ActionPlanReviewsResponse::class.java)
      .responseBody.blockFirst()!!

  internal fun HmppsQueue.receiveEvent(queueType: QueueType): HmppsDomainEvent {
    val event = receiveEventsOnQueue(queueType).single()
    sqsClient.purgeQueue { it.queueUrl(queueType.queueUrl) }
    return event
  }

  internal fun HmppsQueue.receiveEventsOnQueue(queueType: QueueType, maxMessages: Int = 10): List<HmppsDomainEvent> {
    val messageCount = when (queueType) {
      QueueType.INDUCTION -> inductionScheduleEventQueue.countAllMessagesOnQueue()
      QueueType.REVIEW -> reviewScheduleEventQueue.countAllMessagesOnQueue()
    }

    val queueUrl = when (queueType) {
      QueueType.INDUCTION -> inductionScheduleEventQueue.queueUrl
      QueueType.REVIEW -> reviewScheduleEventQueue.queueUrl
    }
    await untilCallTo { messageCount } matches { (it ?: 0) > 0 }
    return sqsClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .maxNumberOfMessages(maxMessages)
        .build(),
    ).get().messages().map { objectMapper.readValue<Notification>(it.body()) }
      .map { objectMapper.readValue<HmppsDomainEvent>(it.message) }
  }

  enum class QueueType(val queueUrl: String) {
    INDUCTION("inductionscheduleeventqueue"),
    REVIEW("reviewQueueUrl"),
  }

  internal fun HmppsQueue.countAllMessagesOnQueue() = sqsClient.countAllMessagesOnQueue(queueUrl).get()
  fun createInductionSchedule(
    prisonNumber: String,
    status: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
  ) {
    inductionScheduleRepository.save(
      InductionScheduleEntity(
        prisonNumber = prisonNumber,
        deadlineDate = LocalDate.now().plusMonths(1),
        reference = UUID.randomUUID(),
        scheduleStatus = status,
        scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
      ),
    )
  }

  fun createReviewScheduleRecord(
    prisonNumber: String,
    status: String = ReviewScheduleStatus.SCHEDULED.name,
    earliestDate: LocalDate = LocalDate.now().minusMonths(1),
    latestDate: LocalDate = LocalDate.now().plusMonths(1),
  ) {
    val reviewScheduleEntity = ReviewScheduleEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      earliestReviewDate = earliestDate,
      latestReviewDate = latestDate,
      scheduleCalculationRule = ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
      scheduleStatus = ReviewScheduleStatus.valueOf(status),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    reviewScheduleRepository.saveAndFlush(reviewScheduleEntity)
  }
}

data class Notification(
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
)

data class MessageAttributes(
  @JsonAnyGetter @JsonAnySetter
  private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {
  override operator fun get(key: String): MessageAttribute? = attributes[key]
  operator fun set(key: String, value: MessageAttribute) {
    attributes[key] = value
  }
}

data class MessageAttribute(@JsonProperty("Type") val type: String, @JsonProperty("Value") val value: String)
