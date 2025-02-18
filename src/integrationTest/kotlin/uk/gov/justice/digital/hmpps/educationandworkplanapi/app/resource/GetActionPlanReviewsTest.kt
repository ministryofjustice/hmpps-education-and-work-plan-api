package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithNoAuthorities
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.EntityType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.note.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import java.time.LocalDate
import java.util.UUID

class GetActionPlanReviewsTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews"
  }

  private val prisonNumber = aValidPrisonNumber()

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token without required role`() {
    // Given

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithNoAuthorities(privateKey = keyPair.private))
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
  fun `should return not found given prisoner does not have a review schedule`() {
    // Given

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          REVIEWS_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isNotFound
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.NOT_FOUND.value())
      .hasUserMessage("Review Schedule not found for prisoner [$prisonNumber]")
  }

  @Test
  fun `should return prisoner review schedule and completed reviews`() {
    // Given
    val reviewSchedule = createReviewScheduleRecords(prisonNumber)
    createCompletedReviewRecord(prisonNumber, reviewSchedule.reference)

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(
        aValidTokenWithAuthority(
          REVIEWS_RO,
          privateKey = keyPair.private,
        ),
      )
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ActionPlanReviewsResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    // TODO - enhance assertions to assert detail of the response once we have service code/REST APIs to create/submit reviews etc
    assertThat(actual).isNotNull()
    assertThat(actual!!.latestReviewSchedule).isNotNull()
    assertThat(actual.completedReviews).size().isEqualTo(1)
  }

  // TODO - replace with a relevant service call once we have that developed
  private fun createReviewScheduleRecords(prisonNumber: String): ReviewScheduleEntity = (1..3).map {
    val reviewScheduleEntity = ReviewScheduleEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      earliestReviewDate = LocalDate.now().minusMonths(1),
      latestReviewDate = LocalDate.now().plusMonths(1),
      scheduleCalculationRule = ReviewScheduleCalculationRule.PRISONER_TRANSFER,
      scheduleStatus = if (it == 3) ReviewScheduleStatus.SCHEDULED else ReviewScheduleStatus.COMPLETED,
      exemptionReason = null,
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    reviewScheduleRepository.saveAndFlush(reviewScheduleEntity)
  }.last()

  // TODO - replace with a relevant service call once we have that developed
  private fun createCompletedReviewRecord(prisonNumber: String, reviewScheduleReference: UUID): ReviewEntity {
    val reviewReference = UUID.randomUUID()
    val completedReview = ReviewEntity(
      reference = reviewReference,
      prisonNumber = prisonNumber,
      deadlineDate = LocalDate.now().minusMonths(12),
      completedDate = LocalDate.now().minusMonths(13),
      reviewScheduleReference = reviewScheduleReference,
      conductedBy = null,
      conductedByRole = null,
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    val completedReviewEntity = reviewRepository.saveAndFlush(completedReview)

    val reviewNoteEntity = NoteEntity(
      reference = UUID.randomUUID(),
      prisonNumber = prisonNumber,
      content = "Peter has made good progress with his goals",
      noteType = NoteType.REVIEW,
      entityType = EntityType.REVIEW,
      entityReference = reviewReference,
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )
    noteRepository.saveAndFlush(reviewNoteEntity)

    return completedReviewEntity
  }
}
