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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule.EXISTING_PRISONER
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidHmppsDomainEventsSqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidPrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.events.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse

@Isolated
@ActiveProfiles("integration-test", "ciag-kpi-pef-rules")
class PrisonerReceivedDueToAdmissionEventTest : IntegrationTestBase() {

  companion object {
    private val TODAY = LocalDate.now()
  }

  // New prison admission, prisoner has never had a PLP
  @Test
  fun `should create new Induction Schedule given prisoner does not already have an Induction and has not had their Screenings and Assessments`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    assertThat(runCatching { getInduction(prisonNumber) }.getOrNull()).isNull()
    assertThat(runCatching { getInductionSchedule(prisonNumber) }.getOrNull()).isNull()

    val earliestDateTime = OffsetDateTime.now()

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = ADMISSION,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      // test induction schedule was created
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
        .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
        .wasStatus(SCHEDULED)
        .hasDeadlineDate(TODAY.plusDays(20))

      // test induction schedule history is created
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
            .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
            .wasStatus(SCHEDULED)
            .hasDeadlineDate(TODAY.plusDays(20))
            .wasVersion(1)
        }

      // test that outbound event is also created:
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
    val prisonNumber = randomValidPrisonNumber()
    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    assertThat(runCatching { getInduction(prisonNumber) }.getOrNull()).isNull()
    assertThat(runCatching { getInductionSchedule(prisonNumber) }.getOrNull()).isNull()

    val earliestDateTime = OffsetDateTime.now()

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
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = ADMISSION,
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      // test induction schedule was created
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
        .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
        .wasStatus(SCHEDULED)
        .hasDeadlineDate(TODAY.plusDays(20))

      // test induction schedule history is created
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
            .wasScheduleCalculationRule(InductionScheduleCalculationRuleResponse.NEW_PRISON_ADMISSION)
            .wasStatus(SCHEDULED)
            .hasDeadlineDate(TODAY.plusDays(20))
            .wasVersion(1)
        }

      // test that outbound event is also created:
      val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
      assertThat(inductionScheduleEvent)
        .hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
        .hasNumberOfPersonReferenceIdentifiers(1)
        .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
    }
  }

  // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction Schedule but it was never completed
  @Test
  fun `should re-schedule the Induction Schedule given prisoner that already has an incomplete Induction Schedule`() {
    // Given
    // an induction schedule is created
    val prisonNumber = randomValidPrisonNumber()
    val originalInductionDueDate = TODAY.minusWeeks(10)
    val inductionScheduleReference = UUID.randomUUID()
    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = originalInductionDueDate,
      createdAtPrison = "BXI",
      inductionScheduleCalculationRule = EXISTING_PRISONER,
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatus.SCHEDULED,
      deadlineDate = originalInductionDueDate,
      createdAtPrison = "BXI",
    )

    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = PRISONER_RECEIVED_INTO_PRISON,
      additionalInformation = aValidPrisonerReceivedAdditionalInformation(
        prisonNumber = prisonNumber,
        reason = ADMISSION,
      ),
    )

    val inductionSchedule = getInductionSchedule(prisonNumber)
    assertThat(inductionSchedule).wasScheduleCalculationRule(InductionScheduleCalculationRule.EXISTING_PRISONER)

    with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
      createPrisonerAPIStub(prisonNumber, this)
    }

    val expectedInductionDueDate = TODAY.plusDays(20) // based on PEF rules as currently set in  application-integration-test.yml

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      val inductionSchedules = getInductionScheduleHistory(prisonNumber)
      assertThat(inductionSchedules)
        .hasNumberOfInductionScheduleVersions(2)
        .inductionScheduleVersion(1) {
          it.wasStatus(SCHEDULED)
            .hasDeadlineDate(originalInductionDueDate)
            .wasCreatedAtPrison("BXI")
            .wasUpdatedAtPrison("BXI")
        }
        .inductionScheduleVersion(2) {
          it.wasStatus(SCHEDULED)
            .hasDeadlineDate(expectedInductionDueDate)
            .wasCreatedAtPrison("BXI")
            .wasUpdatedAtPrison("MDI")
        }

      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule).wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)

      // test that outbound event is also created:
      val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
      assertThat(inductionScheduleEvent)
        .hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
        .hasNumberOfPersonReferenceIdentifiers(1)
        .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
    }
  }
}
