package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewSchedulesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import java.time.Instant
import java.util.UUID

class GetReviewSchedulesTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews/review-schedules"
  }

  private val prisonNumber = randomValidPrisonNumber()

  @Test
  fun `should return unauthorized given no bearer token`() {
    webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden given bearer token with wrong role`() {
    // Given

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(INDUCTIONS_RO, privateKey = keyPair.private))
      .exchange()
      .expectStatus()
      .isForbidden
      .returnResult(ErrorResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()
    assertThat(actual)
      .hasStatus(HttpStatus.FORBIDDEN.value())
      .hasUserMessage("Access Denied")
      .hasDeveloperMessage("Access denied on uri=/action-plans/$prisonNumber/reviews/review-schedules")
  }

  @Test
  fun `should return a list of review schedules`() {
    // Given
    val reference1 = UUID.randomUUID()
    createReviewScheduleHistoryRecord(prisonNumber = prisonNumber, version = 1, status = SCHEDULED, reference = reference1, updatedAt = Instant.now().minusSeconds(6))
    createReviewScheduleHistoryRecord(prisonNumber = prisonNumber, version = 2, status = COMPLETED, reference = reference1, updatedAt = Instant.now().minusSeconds(5))

    val reference2 = UUID.randomUUID()
    createReviewScheduleHistoryRecord(prisonNumber = prisonNumber, version = 1, status = SCHEDULED, reference = reference2, updatedAt = Instant.now().minusSeconds(4))
    createReviewScheduleHistoryRecord(prisonNumber = prisonNumber, version = 2, status = EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY, reference = reference2, updatedAt = Instant.now().minusSeconds(3))
    createReviewScheduleHistoryRecord(prisonNumber = prisonNumber, version = 3, status = SCHEDULED, reference = reference2, updatedAt = Instant.now().minusSeconds(3))
    createReviewScheduleHistoryRecord(prisonNumber = prisonNumber, version = 4, status = COMPLETED, reference = reference2, updatedAt = Instant.now().minusSeconds(1))

    // When
    val response = webTestClient.get()
      .uri(URI_TEMPLATE, prisonNumber)
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk
      .returnResult(ActionPlanReviewSchedulesResponse::class.java)

    // Then
    val actual = response.responseBody.blockFirst()!!
    assertThat(actual.reviewSchedules).hasSize(6)

    val statuses = actual.reviewSchedules.map { it.status.name }
    val expectedStatuses = listOf("COMPLETED", "SCHEDULED", "EXEMPT_PRISONER_DRUG_OR_ALCOHOL_DEPENDENCY", "SCHEDULED", "COMPLETED", "SCHEDULED")
    assertThat(statuses).isEqualTo(expectedStatuses)
    assertThat(actual.reviewSchedules[0].reference).isEqualTo(reference2)
    assertThat(actual.reviewSchedules[1].reference).isEqualTo(reference2)
    assertThat(actual.reviewSchedules[2].reference).isEqualTo(reference2)
    assertThat(actual.reviewSchedules[3].reference).isEqualTo(reference2)

    assertThat(actual.reviewSchedules[4].reference).isEqualTo(reference1)
    assertThat(actual.reviewSchedules[5].reference).isEqualTo(reference1)
  }
}
