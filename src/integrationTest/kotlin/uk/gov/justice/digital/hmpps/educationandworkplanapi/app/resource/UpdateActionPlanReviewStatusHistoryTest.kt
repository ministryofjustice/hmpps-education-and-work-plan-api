package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidUpdateReviewScheduleStatusRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate

@Isolated
class UpdateActionPlanReviewStatusHistoryTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews/schedule-status"
  }

  private val prisonNumber = aValidPrisonNumber()

  @Test
  fun `Test that review schedule history records are written when a schedule is exempted`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatus.SCHEDULED.name,
      earliestDate = LocalDate.now().minusDays(5),
      latestDate = LocalDate.now(),
    )

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    val histories = reviewScheduleHistoryRepository.findAllByReference(reviewSchedule[0].reference)
    assertThat(histories.size).isEqualTo(2)
    assertThat(histories[0].scheduleStatus).isEqualTo(uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.EXEMPT_SYSTEM_TECHNICAL_ISSUE)
    assertThat(histories[1].scheduleStatus).isEqualTo(uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus.SCHEDULED)
  }
}
