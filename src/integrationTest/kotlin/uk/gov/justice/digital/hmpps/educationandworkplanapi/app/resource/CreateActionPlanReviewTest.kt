package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.LegalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidCreateActionPlanReviewRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleCalculationRule as ReviewScheduleCalculationRuleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus as ReviewScheduleStatusEntity
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
    val prisonerFromApi = Prisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusYears(1),
      prisonId = "BXI",
    )
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

    val prisonerFromApi = Prisoner(
      prisonerNumber = prisonNumber,
      legalStatus = LegalStatus.SENTENCED,
      releaseDate = LocalDate.now().plusYears(1),
      prisonId = "BXI",
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
        it.wasCreatedAfter(earliestCreationTime)
          .wasCreatedBy("auser_gen")
          .wasCreatedByDisplayName("Albert User")
          .wasCreatedAtPrison("MDI")
          .wasUpdatedAfter(earliestCreationTime)
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
        it.wasCreatedAfter(earliestCreationTime)
          .wasCreatedBy("auser_gen")
          .wasCreatedByDisplayName("Albert User")
          .wasCreatedAtPrison("MDI")
          .wasUpdatedAfter(earliestCreationTime)
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
        it.wasCreatedAfter(earliestCreationTime)
          .wasCreatedBy("auser_gen")
          .wasCreatedByDisplayName("Albert User")
          .wasCreatedAtPrison("MDI")
          .wasCompletedOn(LocalDate.now())
          .hadDeadlineDateOf(LocalDate.now().plusMonths(1))
          .wasConductedBy("Barnie Jones")
          .wasConductedByRole("Peer mentor")
          .note {
            it.wasCreatedAfter(earliestCreationTime)
              .wasCreatedBy("auser_gen")
              .wasCreatedByDisplayName("Albert User")
              .wasCreatedAtPrison("MDI")
              .hasType(NoteType.REVIEW)
              .hasContent("A great review today; prisoner is making good progress towards his goals")
          }
      }
  }

  // TODO - replace with a relevant service call once we have that developed
  private fun createReviewScheduleRecord(prisonNumber: String) {
    val reviewScheduleEntity = ReviewScheduleEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      earliestReviewDate = LocalDate.now().minusMonths(1),
      latestReviewDate = LocalDate.now().plusMonths(1),
      scheduleCalculationRule = ReviewScheduleCalculationRuleEntity.BETWEEN_12_AND_60_MONTHS_TO_SERVE,
      scheduleStatus = ReviewScheduleStatusEntity.SCHEDULED,
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    reviewScheduleRepository.saveAndFlush(reviewScheduleEntity)
  }
}
