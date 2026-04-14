package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusApi
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus as ReviewScheduleStatusApi

@Isolated
class ScheduleEtlTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    clearDatabase()
  }

  @Nested
  inner class CorrectInductionSchedules {
    val uri = "/action-plans/schedules/reschedule-inductions-following-transfer"

    @Test
    fun `should correct induction schedule`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val inductionScheduleReference = UUID.randomUUID()
      createInductionScheduleHistory(
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.SCHEDULED,
        reference = inductionScheduleReference,
        version = 1,
      )
      createInductionScheduleHistory(
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER,
        reference = inductionScheduleReference,
        version = 2,
      )
      createInductionScheduleHistory(
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.SCHEDULED,
        deadlineDate = LocalDate.parse("2026-03-31"),
        reference = inductionScheduleReference,
        version = 3,
      )
      createInductionSchedule(
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.SCHEDULED,
        reference = inductionScheduleReference,
        deadlineDate = LocalDate.parse("2026-03-31"),
      )

      clearQueues()

      val requestBody = PrisonNumbersRequest(prisonNumbers = listOf(prisonNumber))

      // When
      webTestClient.post()
        .uri(uri)
        .withBody(requestBody)
        .bearerToken(aValidTokenWithAuthority(REVIEWS_RW))
        .contentType(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isCreated()

      // Then
      await untilAsserted {
        val inductionSchedule = getInductionSchedule(prisonNumber)
        assertThat(inductionSchedule)
          .hasReference(inductionScheduleReference)
          .wasStatus(InductionScheduleStatusApi.SCHEDULED)
          .hasDeadlineDate(
            LocalDate.now().plusDays(20),
          ) // Deadline should have been extended to date of admission + 20 days, as per standard induction rules for a transfer

        val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
        assertThat(inductionScheduleHistories)
          .hasNumberOfInductionScheduleVersions(5)
          .inductionScheduleVersion(1) {
            it.hasReference(inductionScheduleReference)
              .wasStatus(InductionScheduleStatusApi.SCHEDULED)
          }
          .inductionScheduleVersion(2) {
            it.hasReference(inductionScheduleReference)
              .wasStatus(InductionScheduleStatusApi.EXEMPT_PRISONER_TRANSFER)
          }
          .inductionScheduleVersion(3) {
            it.hasReference(inductionScheduleReference)
              .wasStatus(InductionScheduleStatusApi.SCHEDULED)
              .hasDeadlineDate(LocalDate.parse("2026-03-31")) // The original incorrect deadline date
          }
          .inductionScheduleVersion(4) {
            it.hasReference(inductionScheduleReference)
              .wasStatus(InductionScheduleStatusApi.EXEMPT_PRISONER_TRANSFER)
          }
          .inductionScheduleVersion(5) {
            it.hasReference(inductionScheduleReference)
              .wasStatus(InductionScheduleStatusApi.SCHEDULED)
              .hasDeadlineDate(
                LocalDate.now().plusDays(20),
              ) // Deadline should have been extended to date of admission + 20 days, as per standard induction rules for a transfer
          }

        // test that outbound events are also created:
        val inductionScheduleEvents = domainEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)
        assertThat(inductionScheduleEvents).hasSize(2)
        inductionScheduleEvents.onEach {
          assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
          assertThat(it.detailUrl).isEqualTo("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
        }
      }
    }

    @Test
    fun `should gracefully handle prisoners whose induction schedules do not need correcting`() {
      // Given
      createInductionScheduleHistory(
        prisonNumber = "A1234BB",
        status = InductionScheduleStatus.SCHEDULED,
        reference = UUID.randomUUID(),
        version = 1,
      )

      var inductionScheduleReference = UUID.randomUUID()
      createInductionScheduleHistory(
        prisonNumber = "A1234CC",
        status = InductionScheduleStatus.SCHEDULED,
        reference = inductionScheduleReference,
        version = 1,
      )
      createInductionScheduleHistory(
        prisonNumber = "A1234CC",
        status = InductionScheduleStatus.COMPLETED,
        reference = inductionScheduleReference,
        version = 2,
      )

      inductionScheduleReference = UUID.randomUUID()
      createInductionScheduleHistory(
        prisonNumber = "A1234DD",
        status = InductionScheduleStatus.SCHEDULED,
        reference = inductionScheduleReference,
        version = 1,
      )
      createInductionScheduleHistory(
        prisonNumber = "A1234DD",
        status = InductionScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
        reference = inductionScheduleReference,
        version = 2,
      )

      inductionScheduleReference = UUID.randomUUID()
      createInductionScheduleHistory(
        prisonNumber = "A1234EE",
        status = InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER,
        reference = inductionScheduleReference,
        version = 1,
      )
      createInductionScheduleHistory(
        prisonNumber = "A1234EE",
        status = InductionScheduleStatus.SCHEDULED,
        deadlineDate = LocalDate.parse("2026-04-18"),
        reference = inductionScheduleReference,
        version = 2,
      )

      val requestBody = PrisonNumbersRequest(
        prisonNumbers = listOf(
          "A1234AA", // Prisoner with no induction schedules
          "A1234BB", // Has only 1 version of the schedule, and it is SCHEDULED
          "A1234CC", // Has a completed Induction
          "A1234DD", // Has an exemption
          "A1234EE", // Was transferred and re-scheduled, but the deadline date is not in the affected date range
        ),
      )

      // When
      webTestClient.post()
        .uri(uri)
        .withBody(requestBody)
        .bearerToken(aValidTokenWithAuthority(REVIEWS_RW))
        .contentType(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isCreated()

      // Then
      verify(inductionScheduleService, never()).exemptAndReScheduleActiveInductionScheduleDueToPrisonerTransfer(
        any(),
        any(),
      )
    }
  }

  @Nested
  inner class CorrectReviewSchedules {
    val uri = "/action-plans/schedules/reschedule-reviews-following-transfer"

    @Test
    fun `should correct review schedule`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val firstReviewScheduleReference = UUID.randomUUID()
      createReviewScheduleHistoryRecord(
        prisonNumber = prisonNumber,
        status = ReviewScheduleStatus.SCHEDULED,
        reference = firstReviewScheduleReference,
        version = 1,
      )
      createReviewScheduleHistoryRecord(
        prisonNumber = prisonNumber,
        status = ReviewScheduleStatus.COMPLETED,
        reference = firstReviewScheduleReference,
        version = 2,
      )
      createReviewScheduleRecord(
        prisonNumber = prisonNumber,
        status = ReviewScheduleStatus.COMPLETED,
        reference = firstReviewScheduleReference,
      )

      val activeReviewScheduleReference = UUID.randomUUID()
      createReviewScheduleHistoryRecord(
        prisonNumber = prisonNumber,
        status = ReviewScheduleStatus.SCHEDULED,
        reference = activeReviewScheduleReference,
        version = 1,
      )
      createReviewScheduleHistoryRecord(
        prisonNumber = prisonNumber,
        status = ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER,
        reference = activeReviewScheduleReference,
        version = 2,
      )
      createReviewScheduleHistoryRecord(
        prisonNumber = prisonNumber,
        status = ReviewScheduleStatus.SCHEDULED,
        latestDate = LocalDate.parse("2026-04-06"),
        reference = activeReviewScheduleReference,
        version = 3,
      )
      createReviewScheduleRecord(
        prisonNumber = prisonNumber,
        status = ReviewScheduleStatus.SCHEDULED,
        reference = activeReviewScheduleReference,
        latestDate = LocalDate.parse("2026-04-06"),
      )

      clearQueues()

      val requestBody = PrisonNumbersRequest(prisonNumbers = listOf(prisonNumber))

      // When
      webTestClient.post()
        .uri(uri)
        .withBody(requestBody)
        .bearerToken(aValidTokenWithAuthority(REVIEWS_RW))
        .contentType(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isCreated()

      // Then
      await untilAsserted {
        val reviews = getActionPlanReviews(prisonNumber)
        assertThat(reviews)
          .latestReviewSchedule {
            it.hasReference(activeReviewScheduleReference)
              .hasStatus(ReviewScheduleStatusApi.SCHEDULED)
              .hasReviewDateTo(
                LocalDate.now().plusDays(10),
              ) // Deadline should have been extended to date of admission + 10 days, as per standard review rules for a transfer
          }

        val reviewScheduleHistories = getReviewSchedules(prisonNumber)
        assertThat(reviewScheduleHistories)
          .hasNumberOfReviewSchedules(7)
          .schedulesForReviewReference(firstReviewScheduleReference) {
            it.hasNumberOfReviewSchedules(2)
              .reviewScheduleAtVersion(1) {
                it.hasStatus(ReviewScheduleStatusApi.SCHEDULED)
              }
              .reviewScheduleAtVersion(2) {
                it.hasStatus(ReviewScheduleStatusApi.COMPLETED)
              }
          }
          .schedulesForReviewReference(activeReviewScheduleReference) {
            it.hasNumberOfReviewSchedules(5)
              .reviewScheduleAtVersion(1) {
                it.hasStatus(ReviewScheduleStatusApi.SCHEDULED)
              }
              .reviewScheduleAtVersion(2) {
                it.hasStatus(ReviewScheduleStatusApi.EXEMPT_PRISONER_TRANSFER)
              }
              .reviewScheduleAtVersion(3) {
                it.hasStatus(ReviewScheduleStatusApi.SCHEDULED)
                  .hasReviewDateTo(LocalDate.parse("2026-04-06")) // The original incorrect deadline date
              }
              .reviewScheduleAtVersion(4) {
                it.hasStatus(ReviewScheduleStatusApi.EXEMPT_PRISONER_TRANSFER)
              }
              .reviewScheduleAtVersion(5) {
                it.hasStatus(ReviewScheduleStatusApi.SCHEDULED)
                  .hasReviewDateTo(
                    LocalDate.now().plusDays(10),
                  ) // Deadline should have been extended to date of admission + 10 days, as per standard induction rules for a transfer
              }
          }

        // test that outbound events are also created:
        val reviewScheduleEvents = domainEventQueue.receiveEventsOnQueue(QueueType.REVIEW)
        assertThat(reviewScheduleEvents).hasSize(2)
        reviewScheduleEvents.onEach {
          assertThat(it.personReference.identifiers[0].value).isEqualTo(prisonNumber)
          assertThat(it.detailUrl).isEqualTo("http://localhost:8080/reviews/$prisonNumber/review-schedule")
        }
      }
    }

    @Test
    fun `should gracefully handle prisoners whose review schedules do not need correcting`() {
      // Given
      createReviewScheduleHistoryRecord(
        prisonNumber = "A1234BB",
        status = ReviewScheduleStatus.SCHEDULED,
        reference = UUID.randomUUID(),
        version = 1,
      )

      var reviewScheduleReference = UUID.randomUUID()
      createReviewScheduleHistoryRecord(
        prisonNumber = "A1234CC",
        status = ReviewScheduleStatus.SCHEDULED,
        reference = reviewScheduleReference,
        version = 1,
      )
      createReviewScheduleHistoryRecord(
        prisonNumber = "A1234CC",
        status = ReviewScheduleStatus.COMPLETED,
        reference = reviewScheduleReference,
        version = 2,
      )
      createReviewScheduleHistoryRecord(
        prisonNumber = "A1234CC",
        status = ReviewScheduleStatus.SCHEDULED,
        reference = UUID.randomUUID(),
        version = 1,
      )

      reviewScheduleReference = UUID.randomUUID()
      createReviewScheduleHistoryRecord(
        prisonNumber = "A1234DD",
        status = ReviewScheduleStatus.SCHEDULED,
        reference = reviewScheduleReference,
        version = 1,
      )
      createReviewScheduleHistoryRecord(
        prisonNumber = "A1234DD",
        status = ReviewScheduleStatus.EXEMPT_PRISONER_SAFETY_ISSUES,
        reference = reviewScheduleReference,
        version = 2,
      )

      reviewScheduleReference = UUID.randomUUID()
      createReviewScheduleHistoryRecord(
        prisonNumber = "A1234EE",
        status = ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER,
        reference = reviewScheduleReference,
        version = 1,
      )
      createReviewScheduleHistoryRecord(
        prisonNumber = "A1234EE",
        status = ReviewScheduleStatus.SCHEDULED,
        latestDate = LocalDate.parse("2026-04-18"),
        reference = reviewScheduleReference,
        version = 2,
      )

      val requestBody = PrisonNumbersRequest(
        prisonNumbers = listOf(
          "A1234AA", // Prisoner with no review schedules
          "A1234BB", // Has only 1 version of the schedule, and it is SCHEDULED
          "A1234CC", // Has a completed Review
          "A1234DD", // Has an exemption
          "A1234EE", // Was transferred and re-scheduled, but the deadline date is not in the affected date range
        ),
      )

      // When
      webTestClient.post()
        .uri(uri)
        .withBody(requestBody)
        .bearerToken(aValidTokenWithAuthority(REVIEWS_RW))
        .contentType(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isCreated()

      // Then
      verify(reviewScheduleService, never()).exemptAndReScheduleActiveReviewScheduleDueToPrisonerTransfer(
        any(),
        any(),
      )
    }
  }
}
