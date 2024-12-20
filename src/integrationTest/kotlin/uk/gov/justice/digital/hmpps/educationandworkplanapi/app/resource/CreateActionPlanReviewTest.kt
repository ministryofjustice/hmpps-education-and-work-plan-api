package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.LegalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
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

  private val prisonNumber = aValidPrisonNumber()

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
      .hasUserMessage("Review Schedule not found for prisoner [A1234BC]")
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

    createReviewScheduleRecord(prisonNumber)

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

    await.untilAsserted {
      val timeline = getTimeline(prisonNumber)
      assertThat(timeline)
        .event(1) {
          it.hasEventType(TimelineEventType.ACTION_PLAN_REVIEW_COMPLETED)
            .wasActionedBy("auser_gen")
            .hasActionedByDisplayName("Albert User")
        }

      val eventPropertiesCaptor = ArgumentCaptor.forClass(Map::class.java as Class<Map<String, String>>)

      verify(telemetryClient).trackEvent(
        eq("REVIEW_COMPLETED"),
        capture(eventPropertiesCaptor),
        eq(null),
      )

      val reviewCompleteEventProperties = eventPropertiesCaptor.firstValue
      assertThat(reviewCompleteEventProperties)
        .containsEntry("reference", reviews.completedReviews.first().reference.toString())
    }
  }
}
