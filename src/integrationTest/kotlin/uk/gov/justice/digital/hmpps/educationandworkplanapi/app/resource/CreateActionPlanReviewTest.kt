package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.LegalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidCreateActionPlanReviewRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import java.time.OffsetDateTime
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleCalculationRule as ReviewScheduleCalculationRuleApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus as ReviewScheduleStatusApi

class CreateActionPlanReviewTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews"
  }

  private val prisonNumber = randomValidPrisonNumber()

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .contentType(APPLICATION_JSON)
      .withBody(aValidCreateActionPlanReviewRequest())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with view only role`() {
    // Given

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .contentType(APPLICATION_JSON)
      .withBody(aValidCreateActionPlanReviewRequest())
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RO, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/action-plans/$prisonNumber/reviews")
  }

  @Test
  fun `should fail to create review given no review data provided`() {
    // Given

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .bodyValue(
        """
          { }
        """.trimIndent(),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isBadRequest
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(BAD_REQUEST.value())
      .hasUserMessageContaining("JSON parse error")
      .hasUserMessageContaining("value failed for JSON property prisonId due to missing (therefore NULL) value for creator parameter prisonId")
  }

  @Test
  fun `should fail to create review given prisoner does not exist`() {
    // Given
    wiremockService.stubGetPrisonerNotFound(prisonNumber)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(aValidCreateActionPlanReviewRequest())
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(NOT_FOUND.value())
      .hasUserMessage("Prisoner [$prisonNumber] not returned by Prisoner Search API")
  }

  @Test
  fun `should fail to create review given prisoner does not have a review schedule`() {
    // Given
    val prisonerFromApi = aValidPrisoner(prisonerNumber = prisonNumber)
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(aValidCreateActionPlanReviewRequest())
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(NOT_FOUND.value())
      .hasUserMessage("Review Schedule not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should create review and new review schedule given prisoner has a review schedule`() {
    // Given
    val earliestCreationTime = OffsetDateTime.now()

    val prisonerFromApi = aValidPrisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusYears(1),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    val reviewSchedule = createReviewScheduleRecord(prisonNumber)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidCreateActionPlanReviewRequest(
          prisonId = "MDI",
          conductedBy = "Barnie Jones",
          conductedByRole = "Peer mentor",
          note = "A great review today; prisoner is making good progress towards his goals",
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated
      .returnResult(CreateActionPlanReviewResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .wasNotLastReviewBeforeRelease()
      .latestReviewSchedule {
        it.wasCreatedAtOrAfter(earliestCreationTime)
          .wasCreatedBy("auser_gen")
          .wasCreatedByDisplayName("Albert User")
          .wasCreatedAtPrison("MDI")
          .wasUpdatedAtOrAfter(earliestCreationTime)
          .wasUpdatedBy("auser_gen")
          .wasUpdatedByDisplayName("Albert User")
          .wasUpdatedAtPrison("MDI")
          .hasStatus(ReviewScheduleStatusApi.SCHEDULED)
          // Prisoner's release date is 1 year away, so next review is between 2 and 3 months from today, based on the calculation rule BETWEEN_6_AND_12_MONTHS_TO_SERVE
          .hasReviewDateFrom(LocalDate.now().plusMonths(2))
          .hasReviewDateTo(LocalDate.now().plusMonths(3))
          .hasCalculationRule(ReviewScheduleCalculationRuleApi.BETWEEN_6_AND_12_MONTHS_TO_SERVE)
      }

    val reviews = getActionPlanReviews(prisonNumber)
    assertThat(reviews)
      .latestReviewSchedule {
        it.wasCreatedAtOrAfter(earliestCreationTime)
          .wasCreatedBy("auser_gen")
          .wasCreatedByDisplayName("Albert User")
          .wasCreatedAtPrison("MDI")
          .wasUpdatedAtOrAfter(earliestCreationTime)
          .wasUpdatedBy("auser_gen")
          .wasUpdatedByDisplayName("Albert User")
          .wasUpdatedAtPrison("MDI")
          .hasStatus(ReviewScheduleStatusApi.SCHEDULED)
          // Prisoner's release date is 1 year away, so next review is between 2 and 3 months from today, based on the calculation rule BETWEEN_6_AND_12_MONTHS_TO_SERVE
          .hasReviewDateFrom(LocalDate.now().plusMonths(2))
          .hasReviewDateTo(LocalDate.now().plusMonths(3))
          .hasCalculationRule(ReviewScheduleCalculationRuleApi.BETWEEN_6_AND_12_MONTHS_TO_SERVE)
      }
      .hasNumberOfCompletedReviews(1)
      .completedReview(1) {
        it.wasCreatedAtOrAfter(earliestCreationTime)
          .wasCreatedBy("auser_gen")
          .wasCreatedByDisplayName("Albert User")
          .wasCreatedAtPrison("MDI")
          .wasCompletedOn(LocalDate.now())
          .hadDeadlineDateOf(LocalDate.now().plusMonths(1))
          .wasConductedBy("Barnie Jones")
          .wasConductedByRole("Peer mentor")
          .note {
            it.wasCreatedAtOrAfter(earliestCreationTime)
              .wasCreatedBy("auser_gen")
              .wasCreatedByDisplayName("Albert User")
              .wasCreatedAtPrison("MDI")
              .hasType(NoteType.REVIEW)
              .hasContent("A great review today; prisoner is making good progress towards his goals")
          }
      }

    // Test that the completed review has the correct review schedule reference
    assertThat(reviews.completedReviews[0].reviewScheduleReference).isEqualTo(reviewSchedule.reference)

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .event(1) {
          it.hasEventType(TimelineEventType.ACTION_PLAN_REVIEW_COMPLETED)
            .wasActionedBy("auser_gen")
            .hasActionedByDisplayName("Albert User")
        }

      val eventPropertiesCaptor = createCaptor<Map<String, String>>()

      verify(telemetryClient).trackEvent(
        eq("REVIEW_COMPLETED"),
        capture(eventPropertiesCaptor),
        isNull(),
      )

      val reviewCompleteEventProperties = eventPropertiesCaptor.firstValue
      assertThat(reviewCompleteEventProperties)
        .containsEntry("reference", reviews.completedReviews.first().reference.toString())
    }
  }

  @Test
  fun `should create a pre release review`() {
    // Given
    val earliestCreationTime = OffsetDateTime.now()
    val prisonerNumber = randomValidPrisonNumber()

    val prisonerFromApi = aValidPrisoner(
      prisonerNumber = prisonerNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusMonths(1),
    )
    wiremockService.stubGetPrisonerFromPrisonerSearchApi(prisonNumber, prisonerFromApi)

    val reviewSchedule = createReviewScheduleRecord(prisonNumber)

    // When
    val response = webTestClient.post()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidCreateActionPlanReviewRequest(
          prisonId = "MDI",
          conductedBy = "Barnie Jones",
          conductedByRole = "Peer mentor",
          note = "A great review today; prisoner is making good progress towards his goals",
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isCreated
      .returnResult(CreateActionPlanReviewResponse::class.java)

    // Then
    val reviews = getActionPlanReviews(prisonNumber)
    assertThat(reviews)
      .hasNumberOfCompletedReviews(1)
      .completedReview(1) {
        it.wasPreRelease()
        it.wasCreatedAtOrAfter(earliestCreationTime)
          .wasCreatedBy("auser_gen")
          .wasCreatedByDisplayName("Albert User")
          .wasCreatedAtPrison("MDI")
          .wasCompletedOn(LocalDate.now())
          .hadDeadlineDateOf(LocalDate.now().plusMonths(1))
          .wasConductedBy("Barnie Jones")
          .wasConductedByRole("Peer mentor")
          .note {
            it.wasCreatedAtOrAfter(earliestCreationTime)
              .wasCreatedBy("auser_gen")
              .wasCreatedByDisplayName("Albert User")
              .wasCreatedAtPrison("MDI")
              .hasType(NoteType.REVIEW)
              .hasContent("A great review today; prisoner is making good progress towards his goals")
          }
      }
  }

  @Nested
  @DisplayName("Given upstream error (from Prisoner Search)")
  inner class GivenUpstreamError {
    private val maxAttempts = apiClientMaxAttempts

    private lateinit var prisonNumber: String
    private lateinit var prisoner: Prisoner
    private lateinit var dpsUsername: String
    private lateinit var dpsUserDisplayName: String
    private lateinit var earliestCreationTime: OffsetDateTime

    @BeforeEach
    internal fun setUp() {
      // Given for each test
      earliestCreationTime = OffsetDateTime.now()
      prisonNumber = randomValidPrisonNumber()
      prisoner = aValidPrisoner(
        prisonerNumber = prisonNumber,
        legalStatus = LegalStatus.SENTENCED,
        releaseDate = LocalDate.now().plusYears(1),
      )
      dpsUsername = "auser_gen"
      dpsUserDisplayName = "Albert User"
    }

    @Nested
    @DisplayName("And prisoner has a review schedule")
    inner class AndAReviewSchedule {
      private lateinit var reviewSchedule: ReviewScheduleEntity

      @BeforeEach
      internal fun setUp() {
        // Given for each test
        reviewSchedule = createReviewScheduleRecord(prisonNumber)
      }

      @Test
      fun `should create a review, given earlier connection reset by peer (RST)`() {
        // Given
        // connection reset errors, before that last successful call (4th)
        val numberOfRequests = 4
        wiremockService.stubGetPrisonerWithEarlierConnectionResetError(prisonNumber, prisoner, numberOfRequests)
        val createRequest = aValidCreateActionPlanReviewRequest()

        // When
        val response = createActionPlanReviewIsCreated(prisonNumber, createRequest, dpsUsername)
          .returnCreationResponse()

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual).isNotNull
        assertReviewCreated(dpsUsername, dpsUserDisplayName, earliestCreationTime, reviewSchedule)
        wiremockService.verifyGetPrisoner(numberOfRequests)
      }

      @Test
      fun `should create a review, given earlier connection timed out`() {
        // Given
        val numberOfRequests = 4
        // connection timed out errors, before that last successful call (4th)
        wiremockService.stubGetPrisonerWithEarlierConnectionTimedOutError(prisonNumber, prisoner, numberOfRequests)
        val createRequest = aValidCreateActionPlanReviewRequest()

        // When
        val response = createActionPlanReviewIsCreated(prisonNumber, createRequest, dpsUsername)
          .returnCreationResponse()

        // Then
        val actual = response.responseBody.blockFirst()
        assertThat(actual).isNotNull
        assertReviewCreated(dpsUsername, dpsUserDisplayName, earliestCreationTime, reviewSchedule)
        wiremockService.verifyGetPrisoner(numberOfRequests)
      }

      @Test
      fun `should fail to create review, given the upstream error remains`() {
        // Given
        val expectedAttempts = maxAttempts
        // upstream error remains at all time connection
        wiremockService.stubGetPrisonerWithConnectionResetError(prisonNumber)
        val createRequest = aValidCreateActionPlanReviewRequest()

        // When
        val response = createActionPlanReviewIsAsExpected(prisonNumber, createRequest, aReadWriteBearerToken(dpsUsername))
          .expectStatus().is5xxServerError
          .returnError()

        // Then
        val actual = response.body()
        assertThat(actual)
          .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
        wiremockService.verifyGetPrisoner(expectedAttempts)
      }
    }

    private fun assertReviewCreated(
      username: String,
      userDisplayName: String,
      earliestCreationTime: OffsetDateTime,
      reviewSchedule: ReviewScheduleEntity,
    ) {
      val reviews = getActionPlanReviews(prisonNumber)
      assertThat(reviews)
        .latestReviewSchedule {
          it.wasCreatedAtOrAfter(earliestCreationTime)
            .wasCreatedBy(username)
            .wasCreatedByDisplayName(userDisplayName)
            .wasUpdatedAtOrAfter(earliestCreationTime)
            .wasUpdatedBy(username)
            .wasUpdatedByDisplayName(userDisplayName)
        }
        .completedReview(1) {
          it.wasCreatedAtOrAfter(earliestCreationTime)
            .wasCreatedBy(username)
            .note {
              it.wasCreatedAtOrAfter(earliestCreationTime)
                .wasCreatedBy(username)
                .wasCreatedByDisplayName(userDisplayName)
            }
        }

      // Test that the completed review has the correct review schedule reference
      assertThat(reviews.completedReviews[0].reviewScheduleReference).isEqualTo(reviewSchedule.reference)

      await.untilAsserted {
        val timeline = getTimeline(prisonNumber)
        assertThat(timeline)
          .event(1) {
            it.hasEventType(TimelineEventType.ACTION_PLAN_REVIEW_COMPLETED)
              .wasActionedBy(username)
              .hasActionedByDisplayName(userDisplayName)
          }

        val eventPropertiesCaptor = createCaptor<Map<String, String>>()

        verify(telemetryClient).trackEvent(
          eq("REVIEW_COMPLETED"),
          capture(eventPropertiesCaptor),
          isNull(),
        )

        val reviewCompleteEventProperties = eventPropertiesCaptor.firstValue
        assertThat(reviewCompleteEventProperties)
          .containsEntry("reference", reviews.completedReviews.first().reference.toString())
      }
    }
  }

  private fun createActionPlanReviewIsAsExpected(
    prisonNumber: String,
    createRequest: Any? = null,
    bearerToken: String? = null,
  ) = webTestClient.post()
    .uri(URI_TEMPLATE, prisonNumber)
    .let { bearerToken?.let { bearerToken -> it.bearerToken(bearerToken) } ?: it }
    .contentType(APPLICATION_JSON)
    .let { responseSpec -> createRequest?.let { responseSpec.withBody(it) } ?: responseSpec }
    .exchange()

  private fun createActionPlanReviewIsCreated(
    prisonNumber: String,
    createRequest: CreateActionPlanReviewRequest,
    username: String,
  ) = createActionPlanReviewIsAsExpected(prisonNumber, createRequest, bearerToken = aReadWriteBearerToken(username))
    .expectStatus().isCreated

  private fun aReadWriteBearerToken(username: String = "auser_gen") = aValidTokenWithAuthority(REVIEWS_RW, username = username, privateKey = keyPair.private)

  private fun WebTestClient.ResponseSpec.returnCreationResponse() = this.returnResult(CreateActionPlanReviewResponse::class.java)
}
