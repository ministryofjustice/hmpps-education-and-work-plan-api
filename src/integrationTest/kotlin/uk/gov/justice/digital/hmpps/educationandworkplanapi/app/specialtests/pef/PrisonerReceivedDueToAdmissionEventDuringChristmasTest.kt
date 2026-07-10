package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests.pef

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidHmppsDomainEventsSqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidPrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.events.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Isolated
@ActiveProfiles("integration-test", "ciag-kpi-pef-rules", "christmas-clock")
class PrisonerReceivedDueToAdmissionEventDuringChristmasTest : IntegrationTestBase() {
  @Test
  fun `should create new Induction Schedule given prisoner does not already have an Induction and has not had their Screenings and Assessments`() {
    // Given
    val today = LocalDate.now(clock)

    val prisonNumber = randomValidPrisonNumber()
    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    assertThat(runCatching { getInduction(prisonNumber) }.getOrNull()).isNull()
    assertThat(runCatching { getInductionSchedule(prisonNumber) }.getOrNull()).isNull()

    val earliestDateTime = OffsetDateTime.now(clock)

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      occurredAt = Instant.now(clock).minusSeconds(10),
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = ADMISSION,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasCreatedAtOrAfter(earliestDateTime)
        .wasUpdatedAtOrAfter(earliestDateTime)
        .wasCreatedBy("system")
        .wasCreatedByDisplayName("system")
        .wasCreatedAtPrison("MDI")
        .wasUpdatedBy("system")
        .wasUpdatedByDisplayName("system")
        .wasUpdatedAtPrison("MDI")
        .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD)
        .wasStatus(SCHEDULED)
        .hasDeadlineDate(today.plusDays(25))

      val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionScheduleHistories)
        .hasNumberOfInductionScheduleVersions(1)
        .inductionScheduleVersion(1) {
          it.wasCreatedAtOrAfter(earliestDateTime)
            .wasUpdatedAtOrAfter(earliestDateTime)
            .wasCreatedBy("system")
            .wasCreatedByDisplayName("system")
            .wasCreatedAtPrison("MDI")
            .wasUpdatedBy("system")
            .wasUpdatedByDisplayName("system")
            .wasUpdatedAtPrison("MDI")
            .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD)
            .wasStatus(SCHEDULED)
            .hasDeadlineDate(today.plusDays(25))
            .wasVersion(1)
        }

      val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
      assertThat(inductionScheduleEvent)
        .hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
        .hasNumberOfPersonReferenceIdentifiers(1)
        .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
    }
  }

  @Test
  fun `should create new Induction Schedule given prisoner does not already have an Induction and has had their Screenings and Assessments completed`() {
    // Given
    val today = LocalDate.now(clock)

    val prisonNumber = randomValidPrisonNumber()
    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    assertThat(runCatching { getInduction(prisonNumber) }.getOrNull()).isNull()
    assertThat(runCatching { getInductionSchedule(prisonNumber) }.getOrNull()).isNull()

    val earliestDateTime = OffsetDateTime.now(clock)

    educationAssessmentEventRepository.save(
      EducationAssessmentEventEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        status = ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now().minusDays(2),
        source = "CURIOUS",
        detailUrl = null,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
      ),
    )

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      occurredAt = Instant.now(clock).minusSeconds(10),
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = ADMISSION,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasCreatedAtOrAfter(earliestDateTime)
        .wasUpdatedAtOrAfter(earliestDateTime)
        .wasCreatedBy("system")
        .wasCreatedByDisplayName("system")
        .wasCreatedAtPrison("MDI")
        .wasUpdatedBy("system")
        .wasUpdatedByDisplayName("system")
        .wasUpdatedAtPrison("MDI")
        .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD)
        .wasStatus(SCHEDULED)
        .hasDeadlineDate(today.plusDays(25))

      val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionScheduleHistories)
        .hasNumberOfInductionScheduleVersions(1)
        .inductionScheduleVersion(1) {
          it.wasCreatedAtOrAfter(earliestDateTime)
            .wasUpdatedAtOrAfter(earliestDateTime)
            .wasCreatedBy("system")
            .wasCreatedByDisplayName("system")
            .wasCreatedAtPrison("MDI")
            .wasUpdatedBy("system")
            .wasUpdatedByDisplayName("system")
            .wasUpdatedAtPrison("MDI")
            .wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION_EXTENDED_DEADLINE_PERIOD)
            .wasStatus(SCHEDULED)
            .hasDeadlineDate(today.plusDays(25))
            .wasVersion(1)
        }

      val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
      assertThat(inductionScheduleEvent)
        .hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
        .hasNumberOfPersonReferenceIdentifiers(1)
        .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
    }
  }
}
