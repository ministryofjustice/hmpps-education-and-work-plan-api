package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests.pef

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.PrisonNumbersRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.REVIEWS_RW
import uk.gov.justice.digital.hmpps.educationandworkplanapi.bearerToken
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.withBody
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusApi

@Isolated
@ActiveProfiles("integration-test", "ciag-kpi-pef-rules")
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
          ) // Deadline should have been extended to date of admission + 20 days, as per PEF induction rules for a transfer

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
              ) // Deadline should have been extended to date of admission + 20 days, as per PEF induction rules for a transfer
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
  }
}
