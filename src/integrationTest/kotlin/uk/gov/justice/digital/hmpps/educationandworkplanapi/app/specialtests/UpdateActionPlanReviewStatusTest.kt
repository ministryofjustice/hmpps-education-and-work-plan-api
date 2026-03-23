package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidTokenWithAuthority
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.REVIEWS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.aValidUpdateReviewScheduleStatusRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus as ReviewScheduleStatusEntity

@Isolated
@ActiveProfiles("integration-test", "extend-exemption-deadline-always")
class UpdateActionPlanReviewStatusTest : IntegrationTestBase() {
  companion object {
    private const val URI_TEMPLATE = "/action-plans/{prisonNumber}/reviews/schedule-status"
    private val today = LocalDate.now()
  }

  private val prisonNumber = randomValidPrisonNumber()

  @Test
  fun `should add 5 days to latest review date when exemption removed given the review was not overdue when the exemption was applied`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE,
      earliestDate = today.minusDays(5),
      latestDate = today, // review is not overdue
    )

    val expectedDeadlineDate = today.plusDays(5)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
    assertThat(reviewSchedule[0].latestReviewDate).isEqualTo(expectedDeadlineDate)
  }

  @Test
  fun `should add 5 days to latest review date when exemption removed given the review was already overdue when the exemption was applied`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatusEntity.EXEMPT_PRISONER_FAILED_TO_ENGAGE,
      earliestDate = today.minusDays(6),
      latestDate = today.minusDays(1), // review is already overdue
    )

    val expectedDeadlineDate = today.plusDays(5)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
    assertThat(reviewSchedule[0].latestReviewDate).isEqualTo(expectedDeadlineDate)
  }

  @Test
  fun `should add 10 days to latest review date when exclusion removed given the review was not overdue when the exclusion was applied`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES,
      earliestDate = today.minusDays(5),
      latestDate = today, // review is not overdue
    )

    val expectedDeadlineDate = today.plusDays(10)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
    assertThat(reviewSchedule[0].latestReviewDate).isEqualTo(expectedDeadlineDate)
  }

  @Test
  fun `should add 10 days to latest review date when exclusion removed given the review was already overdue when the exclusion was applied`() {
    // Given
    createReviewScheduleRecord(
      prisonNumber,
      status = ReviewScheduleStatusEntity.EXEMPT_PRISON_REGIME_CIRCUMSTANCES,
      earliestDate = today.minusDays(6),
      latestDate = today.minusDays(1), // review is already overdue
    )

    val expectedDeadlineDate = today.plusDays(10)

    // When
    webTestClient.put()
      .uri(URI_TEMPLATE, prisonNumber)
      .withBody(
        aValidUpdateReviewScheduleStatusRequest(
          prisonId = "MDI",
          status = ReviewScheduleStatus.SCHEDULED,
        ),
      )
      .bearerToken(aValidTokenWithAuthority(REVIEWS_RW, username = "auser_gen", privateKey = keyPair.private))
      .contentType(APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isNoContent

    // Then
    val reviewSchedule = reviewScheduleRepository.getAllByPrisonNumber(prisonNumber)
    assertThat(reviewSchedule.size).isEqualTo(1)
    assertThat(reviewSchedule[0].scheduleStatus.name).isEqualTo(ReviewScheduleStatus.SCHEDULED.name)
    assertThat(reviewSchedule[0].latestReviewDate).isEqualTo(expectedDeadlineDate)
  }
}
