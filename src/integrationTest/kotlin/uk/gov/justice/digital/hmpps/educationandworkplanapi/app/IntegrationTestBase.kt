package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.applicationinsights.TelemetryClient
import mu.KotlinLogging
import org.awaitility.Awaitility
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentCaptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockReset
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidPrisonerInPrisonSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleHistoryEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleHistoryEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.Companion.STATUSES_FOR_ACTIVE_REVIEWS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EmployabilitySkillRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleHistoryRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.InductionScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.NoteRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PreviousQualificationsRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleHistoryRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.TimelineRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.SqsAssessmentEventMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.SqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.ACTIONPLANS_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.ACTIONPLANS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.EDUCATION_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.EDUCATION_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.GOALS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.INDUCTIONS_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.INDUCTIONS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.REVIEWS_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.REVIEWS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.TIMELINE_RO
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.LocalStackContainer
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.testcontainers.PostgresContainer
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewSchedulesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompleteGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionSchedulesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateReviewScheduleStatusRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.aValidCreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.security.KeyPair
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

private val log = KotlinLogging.logger {}

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient(timeout = "PT5M")
abstract class IntegrationTestBase {
  protected val apiClientMaxRetryAttempts = 3
  protected val apiClientMaxAttempts = 1 + apiClientMaxRetryAttempts

  companion object {
    const val CREATE_GOALS_URI_TEMPLATE = "/action-plans/{prisonNumber}/goals"
    const val CREATE_ACTION_PLAN_URI_TEMPLATE = "/action-plans/{prisonNumber}"
    const val GET_ACTION_PLAN_URI_TEMPLATE = "/action-plans/{prisonNumber}"
    const val GET_ACTION_PLAN_REVIEWS_URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews"
    const val GET_REVIEW_SCHEDULES_URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews/review-schedules"
    const val UPDATE_REVIEW_SCHEDULE_STATUS_URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews/schedule-status"
    const val GET_TIMELINE_URI_TEMPLATE = "/timelines/{prisonNumber}"
    const val INDUCTION_URI_TEMPLATE = "/inductions/{prisonNumber}"
    const val EDUCATION_URI_TEMPLATE = "/person/{prisonNumber}/education"
    const val GET_INDUCTION_SCHEDULE_URI_TEMPLATE = "/inductions/{prisonNumber}/induction-schedule"
    const val GET_INDUCTION_SCHEDULE_HISTORY_URI_TEMPLATE = "/inductions/{prisonNumber}/induction-schedule/history"

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
    Awaitility.setDefaultTimeout(5, SECONDS)
  }

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var wiremockService: WiremockService

  @Autowired
  lateinit var actionPlanRepository: ActionPlanRepository

  @Autowired
  lateinit var employabilitySkillRepository: EmployabilitySkillRepository

  @Autowired
  lateinit var timelineRepository: TimelineRepository

  @Autowired
  lateinit var inductionRepository: InductionRepository

  @Autowired
  lateinit var previousQualificationsRepository: PreviousQualificationsRepository

  @Autowired
  lateinit var reviewRepository: ReviewRepository

  @Autowired
  lateinit var reviewScheduleRepository: ReviewScheduleRepository

  @Autowired
  lateinit var reviewScheduleHistoryRepository: ReviewScheduleHistoryRepository

  @MockitoSpyBean(reset = MockReset.BEFORE)
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
  lateinit var inductionScheduleHistoryRepository: InductionScheduleHistoryRepository

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  val domainEventQueue by lazy {
    hmppsQueueService.findByQueueId("educationandworkplan")
      ?: throw MissingQueueException("HmppsQueue educationandworkplan not found")
  }
  val domainEventQueueClient by lazy { domainEventQueue.sqsClient }
  val domainEventQueueDlqClient by lazy { domainEventQueue.sqsDlqClient }

  val assessmentEventQueue by lazy {
    hmppsQueueService.findByQueueId("assessmentevents")
      ?: throw MissingQueueException("HmppsQueue assessmentevents not found")
  }
  val assessmentEventQueueClient by lazy { assessmentEventQueue.sqsClient }

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
  fun reset() {
    clearQueues()
    wiremockService.resetAllStubsAndMappings()
  }

  /**
   * Use this sparingly as it will affect all tests that are running.
   * Also Use @Isolated annotation in the tests that use it.
   */
  fun clearDatabase() {
    actionPlanRepository.deleteAll()
    timelineRepository.deleteAll()
    inductionRepository.deleteAll()
    previousQualificationsRepository.deleteAll()
    reviewRepository.deleteAll()
    reviewScheduleRepository.deleteAll()
    reviewScheduleHistoryRepository.deleteAll()
    noteRepository.deleteAll()
    inductionScheduleRepository.deleteAll()
    inductionScheduleHistoryRepository.deleteAll()
  }

  fun clearQueues() {
    // clear all the queues just in case there are any messages hanging around
    domainEventQueueClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(domainEventQueue.queueUrl).build()).get()
    domainEventQueueDlqClient!!.purgeQueue(PurgeQueueRequest.builder().queueUrl(domainEventQueue.dlqUrl).build())
      .get()

    assessmentEventQueueClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(assessmentEventQueue.queueUrl).build()).get()

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

    testReviewScheduleEventQueueClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(reviewScheduleEventQueue.queueUrl).build(),
    ).get()
    testInductionScheduleEventQueueClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(inductionScheduleEventQueue.queueUrl).build(),
    ).get()
  }

  final inline fun <reified T> createCaptor(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)

  fun getActionPlan(prisonNumber: String): ActionPlanResponse = webTestClient.get()
    .uri(GET_ACTION_PLAN_URI_TEMPLATE, prisonNumber)
    .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RO, privateKey = keyPair.private))
    .exchange()
    .returnResult(ActionPlanResponse::class.java)
    .responseBody.blockFirst()!!

  fun createActionPlan(
    prisonNumber: String,
    createActionPlanRequest: CreateActionPlanRequest = aValidCreateActionPlanRequest(),
    username: String = "auser_gen",
    testSensitiveToGoalCreationOrder: Boolean = false,
  ) {
    if (!testSensitiveToGoalCreationOrder) {
      // Create the action plan including all goals in one operation
      webTestClient.post()
        .uri(CREATE_ACTION_PLAN_URI_TEMPLATE, prisonNumber)
        .withBody(createActionPlanRequest)
        .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private, username = username))
        .contentType(APPLICATION_JSON)
        .exchange()
    } else {
      // The test is sensitive to the order in which the goals are created.
      val goalsToCreate = createActionPlanRequest.goals
      // Create the action plan with the first goal
      webTestClient.post()
        .uri(CREATE_ACTION_PLAN_URI_TEMPLATE, prisonNumber)
        .withBody(createActionPlanRequest.copy(goals = listOf(goalsToCreate.first())))
        .bearerToken(aValidTokenWithAuthority(ACTIONPLANS_RW, privateKey = keyPair.private, username = username))
        .contentType(APPLICATION_JSON)
        .exchange()
      // Then create the remaining goals one at a time in order
      goalsToCreate.slice(1..<goalsToCreate.size).onEach { goalToCreate ->
        createGoals(
          prisonNumber = prisonNumber,
          username = username,
          createGoalsRequest = aValidCreateGoalsRequest(goals = listOf(goalToCreate)),
        )
      }
    }
  }

  fun createGoals(
    prisonNumber: String,
    createGoalsRequest: CreateGoalsRequest = aValidCreateGoalsRequest(),
    username: String = "auser_gen",
  ) {
    webTestClient.post()
      .uri(CREATE_GOALS_URI_TEMPLATE, prisonNumber)
      .withBody(createGoalsRequest)
      .bearerToken(aValidTokenWithAuthority(GOALS_RW, privateKey = keyPair.private, username = username))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  fun getTimeline(prisonNumber: String): TimelineResponse = run {
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

  fun createPrisonerAPIStub(prisonNumber: String, prisoner: Prisoner): Prisoner = run {
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
  ) {
    webTestClient.post()
      .uri(INDUCTION_URI_TEMPLATE, prisonNumber)
      .withBody(createInductionRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          INDUCTIONS_RW,
          privateKey = keyPair.private,
          username = username,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated()
  }

  fun updateReviewStatus(
    prisonNumber: String,
    updateReviewScheduleStatusRequest: UpdateReviewScheduleStatusRequest,
    username: String = "auser_gen",
  ) {
    webTestClient.put()
      .uri(UPDATE_REVIEW_SCHEDULE_STATUS_URI_TEMPLATE, prisonNumber)
      .withBody(updateReviewScheduleStatusRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          REVIEWS_RW,
          privateKey = keyPair.private,
          username = username,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent
  }

  fun getInduction(prisonNumber: String): InductionResponse = webTestClient.get()
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
  ) {
    webTestClient.put()
      .uri("/action-plans/{prisonNumber}/goals/{goalReference}/archive", prisonNumber, archiveGoalRequest.goalReference)
      .withBody(archiveGoalRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
          username = username,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent
  }

  fun completeGoal(
    prisonNumber: String,
    completeGoalRequest: CompleteGoalRequest,
    username: String = "auser_gen",
  ) {
    webTestClient.put()
      .uri("/action-plans/{prisonNumber}/goals/{goalReference}/complete", prisonNumber, completeGoalRequest.goalReference)
      .withBody(completeGoalRequest)
      .bearerToken(
        aValidTokenWithAuthority(
          GOALS_RW,
          privateKey = keyPair.private,
          username = username,
        ),
      )
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent
  }

  fun getEducation(prisonNumber: String): EducationResponse = webTestClient.get()
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

  fun getActionPlanReviews(prisonNumber: String): ActionPlanReviewsResponse = webTestClient.get()
    .uri(GET_ACTION_PLAN_REVIEWS_URI_TEMPLATE, prisonNumber)
    .bearerToken(aValidTokenWithAuthority(REVIEWS_RO, privateKey = keyPair.private))
    .exchange()
    .returnResult(ActionPlanReviewsResponse::class.java)
    .responseBody.blockFirst()!!

  fun getReviewSchedules(prisonNumber: String): ActionPlanReviewSchedulesResponse = webTestClient.get()
    .uri(GET_REVIEW_SCHEDULES_URI_TEMPLATE, prisonNumber)
    .bearerToken(aValidTokenWithAuthority(REVIEWS_RO, privateKey = keyPair.private))
    .exchange()
    .returnResult(ActionPlanReviewSchedulesResponse::class.java)
    .responseBody.blockFirst()!!

  fun getInductionSchedule(prisonNumber: String): InductionScheduleResponse = webTestClient.get()
    .uri(GET_INDUCTION_SCHEDULE_URI_TEMPLATE, prisonNumber)
    .bearerToken(
      aValidTokenWithAuthority(
        INDUCTIONS_RO,
        privateKey = keyPair.private,
      ),
    )
    .exchange()
    .expectStatus()
    .isOk
    .returnResult(InductionScheduleResponse::class.java)
    .responseBody.blockFirst()!!

  fun getInductionScheduleHistory(prisonNumber: String): InductionSchedulesResponse = webTestClient.get()
    .uri(GET_INDUCTION_SCHEDULE_HISTORY_URI_TEMPLATE, prisonNumber)
    .bearerToken(
      aValidTokenWithAuthority(
        INDUCTIONS_RO,
        privateKey = keyPair.private,
      ),
    )
    .exchange()
    .expectStatus()
    .isOk
    .returnResult(InductionSchedulesResponse::class.java)
    .responseBody.blockFirst()!!

  internal fun HmppsQueue.receiveEvent(queueType: QueueType): HmppsDomainEvent = receiveEventsOnQueue(queueType).single()

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
      .also {
        sqsClient.purgeQueue { it.queueUrl(queueType.queueUrl) }
      }
  }

  enum class QueueType(val queueUrl: String) {
    INDUCTION("inductionscheduleeventqueue"),
    REVIEW("reviewQueueUrl"),
  }

  internal fun HmppsQueue.countAllMessagesOnQueue() = sqsClient.countAllMessagesOnQueue(queueUrl).get()

  fun createInductionSchedule(
    prisonNumber: String,
    reference: UUID = UUID.randomUUID(),
    status: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
    deadlineDate: LocalDate = LocalDate.now().plusMonths(1),
    createdAtPrison: String = "BXI",
    updatedAtPrison: String = "BXI",
    exemptionReason: String? = null,
    inductionScheduleCalculationRule: InductionScheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
  ) {
    inductionScheduleRepository.save(
      InductionScheduleEntity(
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        reference = reference,
        scheduleStatus = status,
        scheduleCalculationRule = inductionScheduleCalculationRule,
        createdAtPrison = createdAtPrison,
        updatedAtPrison = updatedAtPrison,
        exemptionReason = exemptionReason,
      ),
    )
  }

  fun createInductionScheduleHistory(
    prisonNumber: String,
    reference: UUID = UUID.randomUUID(),
    status: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
    deadlineDate: LocalDate = LocalDate.now().plusMonths(1),
    version: Int = 1,
    exemptionReason: String? = null,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
    createdAtPrison: String = "BXI",
    updatedAtPrison: String = "BXI",
  ) {
    inductionScheduleHistoryRepository.save(
      InductionScheduleHistoryEntity(
        prisonNumber = prisonNumber,
        deadlineDate = deadlineDate,
        reference = reference,
        scheduleStatus = status,
        scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
        exemptionReason = exemptionReason,
        version = version,
        createdBy = "auser_gen",
        updatedBy = "auser_gen",
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdAtPrison = createdAtPrison,
        updatedAtPrison = updatedAtPrison,
      ),
    )
  }

  fun createReviewScheduleRecord(
    prisonNumber: String,
    status: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
    exemptionReason: String? = null,
    earliestDate: LocalDate = LocalDate.now().minusMonths(1),
    latestDate: LocalDate = LocalDate.now().plusMonths(1),
  ): ReviewScheduleEntity {
    val reviewScheduleEntity = ReviewScheduleEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      earliestReviewDate = earliestDate,
      latestReviewDate = latestDate,
      scheduleCalculationRule = ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
      scheduleStatus = status,
      exemptionReason = exemptionReason,
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    return reviewScheduleRepository.saveAndFlush(reviewScheduleEntity)
  }

  fun createReviewScheduleHistoryRecord(
    prisonNumber: String,
    status: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
    exemptionReason: String? = null,
    earliestDate: LocalDate = LocalDate.now().minusMonths(1),
    latestDate: LocalDate = LocalDate.now().plusMonths(1),
    version: Int = 1,
    reference: UUID = UUID.randomUUID(),
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
  ) {
    val reviewScheduleEntity = ReviewScheduleHistoryEntity(
      reference = reference,
      prisonNumber = prisonNumber,
      earliestReviewDate = earliestDate,
      latestReviewDate = latestDate,
      scheduleCalculationRule = ReviewScheduleCalculationRule.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
      scheduleStatus = status,
      exemptionReason = exemptionReason,
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
      version = version,
      createdBy = "auser_gen",
      updatedBy = "auser_gen",
      createdAt = createdAt,
      updatedAt = updatedAt,
    )
    reviewScheduleHistoryRepository.saveAndFlush(reviewScheduleEntity)
  }

  fun updateReviewScheduleRecordStatus(
    prisonNumber: String,
    status: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
    exemptionReason: String? = null,
  ) {
    val reviewScheduleEntity = reviewScheduleRepository.findByPrisonNumberAndScheduleStatusIn(prisonNumber, STATUSES_FOR_ACTIVE_REVIEWS)
    reviewScheduleEntity?.run {
      scheduleStatus = status
      this.exemptionReason = exemptionReason
      reviewScheduleRepository.saveAndFlush(this)
    }
  }

  fun updateReviewScheduleReviewDates(
    prisonNumber: String,
    earliestReviewDate: LocalDate,
    latestReviewDate: LocalDate,
  ) {
    val reviewScheduleEntity = reviewScheduleRepository.findByPrisonNumberAndScheduleStatusIn(prisonNumber, STATUSES_FOR_ACTIVE_REVIEWS)
    reviewScheduleEntity?.run {
      this.earliestReviewDate = earliestReviewDate
      this.latestReviewDate = latestReviewDate
      reviewScheduleRepository.saveAndFlush(this)
    }
  }

  fun updateInductionScheduleRecordStatus(
    prisonNumber: String,
    status: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
  ) {
    val inductionScheduleEntity = inductionScheduleRepository.findByPrisonNumber(prisonNumber)
    inductionScheduleEntity?.run {
      scheduleStatus = status
      inductionScheduleRepository.saveAndFlush(this)
    }
  }

  fun sendDomainEvent(
    message: SqsMessage,
    queueUrl: String = domainEventQueue.queueUrl,
  ): SendMessageResponse = domainEventQueueClient.sendMessage(
    SendMessageRequest.builder()
      .queueUrl(queueUrl)
      .messageBody(
        objectMapper.writeValueAsString(message),
      ).build(),
  ).get()

  fun sendAssessmentEvent(
    message: SqsAssessmentEventMessage,
    queueUrl: String = assessmentEventQueue.queueUrl,
  ): SendMessageResponse = assessmentEventQueueClient.sendMessage(
    SendMessageRequest.builder()
      .queueUrl(queueUrl)
      .messageBody(
        objectMapper.writeValueAsString(message),
      ).build(),
  ).get()

  fun shortDelay(delay: Long = 200) {
    Thread.sleep(delay)
  }

  fun setUpRandomPrisoner(releaseDate: LocalDate = LocalDate.now().plusYears(1)): String {
    val prisonNumber = randomValidPrisonNumber()
    val prisoner = aValidPrisoner(prisonNumber, releaseDate = releaseDate)
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisoner)
    return prisonNumber
  }

  protected fun WebTestClient.ResponseSpec.returnError() = this.returnResult(ErrorResponse::class.java)
  protected fun <T> FluxExchangeResult<T>.body(): T = this.responseBody.blockFirst()!!
}

data class Notification(
  @param:JsonProperty("Message") val message: String,
  @param:JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
)

data class MessageAttributes(
  @JsonAnyGetter @param:JsonAnySetter
  private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {
  override operator fun get(key: String): MessageAttribute? = attributes[key]
  operator fun set(key: String, value: MessageAttribute) {
    attributes[key] = value
  }
}

data class MessageAttribute(@param:JsonProperty("Type") val type: String, @param:JsonProperty("Value") val value: String)
