package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleCalculationRule.EXISTING_PRISONER
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.events.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus as ReviewScheduleStatusEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusApi

@Isolated
class PrisonerReceivedDueToAdmissionEventTest : IntegrationTestBase() {

  companion object {
    private val TODAY = LocalDate.now()
  }

  @Nested
  @DisplayName("Tests for when the prisoner is admitted/received into prison, where we expect the Induction Schedule to be created or updated")
  inner class InductionSchedule {
    // New prison admission, prisoner has never had a PLP and has not had their screenings and assessments done yet
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
          .wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
          .hasDeadlineDate(TODAY)

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
              .wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
              .hasDeadlineDate(TODAY)
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

    // New prison admission, prisoner has never had a PLP but has had their screenings and assessments done (race condition where the S&A message is processed before the admission message)
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
          .hasDeadlineDate(TODAY.plusDays(10))

        // test induction schedule history is created
        val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
        assertThat(inductionScheduleHistories)
          .hasNumberOfInductionScheduleVersions(2)
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
              .wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
              .hasDeadlineDate(TODAY)
              .wasVersion(1)
          }
          .inductionScheduleVersion(2) {
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
              .hasDeadlineDate(TODAY.plusDays(10))
              .wasVersion(2)
          }
      }

      // test that outbound events are also created:
      val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)
      assertThat(inductionScheduleEvents)
        .hasNumberOfEvents(2)
        .allEvents {
          it.hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
            .hasNumberOfPersonReferenceIdentifiers(1)
            .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
        }
    }

    // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction Schedule but it was never completed
    @ParameterizedTest
    @CsvSource(value = ["EXEMPT_PRISONER_RELEASE", "EXEMPT_PRISONER_RELEASE_HOSPITAL"])
    fun `should re-schedule the Induction Schedule given prisoner that already has an incomplete Induction Schedule and has had their Screenings and Assessments completed`(inductionScheduleStatus: InductionScheduleStatusEntity) {
      // Given
      // an induction schedule is created
      val prisonNumber = randomValidPrisonNumber()
      val originalInductionDueDate = TODAY.minusWeeks(10)
      val inductionScheduleReference = UUID.randomUUID()
      createInductionSchedule(
        reference = inductionScheduleReference,
        prisonNumber = prisonNumber,
        status = inductionScheduleStatus,
        deadlineDate = originalInductionDueDate,
        createdAtPrison = "BXI",
        inductionScheduleCalculationRule = EXISTING_PRISONER,
      )
      createInductionScheduleHistory(
        reference = inductionScheduleReference,
        prisonNumber = prisonNumber,
        status = inductionScheduleStatus,
        deadlineDate = originalInductionDueDate,
        createdAtPrison = "BXI",
      )

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

      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule).wasScheduleCalculationRule(InductionScheduleCalculationRule.EXISTING_PRISONER)

      with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
        createPrisonerAPIStub(prisonNumber, this)
      }

      val expectedInductionDueDate = TODAY.plusDays(10) // based on PES rules

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
          .hasNumberOfInductionScheduleVersions(3)
          .inductionScheduleVersion(1) {
            it.wasStatus(InductionScheduleStatusApi.valueOf(inductionScheduleStatus.toString()))
              .hasDeadlineDate(originalInductionDueDate)
              .wasCreatedAtPrison("BXI")
              .wasUpdatedAtPrison("BXI")
          }
          .inductionScheduleVersion(2) {
            it.wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
              .hasDeadlineDate(TODAY)
              .wasCreatedAtPrison("BXI")
              .wasUpdatedAtPrison("MDI")
          }
          .inductionScheduleVersion(3) {
            it.wasStatus(SCHEDULED)
              .hasDeadlineDate(expectedInductionDueDate)
              .wasCreatedAtPrison("BXI")
              .wasUpdatedAtPrison("MDI")
          }

        val inductionSchedule = getInductionSchedule(prisonNumber)
        assertThat(inductionSchedule).wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)

        // test that outbound events are also created:
        val inductionScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.INDUCTION)
        assertThat(inductionScheduleEvents)
          .hasNumberOfEvents(2)
          .allEvents {
            it.hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
              .hasNumberOfPersonReferenceIdentifiers(1)
              .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
          }
      }
    }

    @ParameterizedTest
    @CsvSource(value = ["EXEMPT_PRISONER_RELEASE", "EXEMPT_PRISONER_RELEASE_HOSPITAL"])
    fun `should re-schedule the Induction Schedule given prisoner that already has an incomplete Induction Schedule and has not had their Screenings and Assessments completed`(inductionScheduleStatus: InductionScheduleStatusEntity) {
      // Given
      // an induction schedule is created
      val prisonNumber = randomValidPrisonNumber()
      val originalInductionDueDate = TODAY.minusWeeks(10)
      val inductionScheduleReference = UUID.randomUUID()
      createInductionSchedule(
        reference = inductionScheduleReference,
        prisonNumber = prisonNumber,
        status = inductionScheduleStatus,
        deadlineDate = originalInductionDueDate,
        createdAtPrison = "BXI",
        inductionScheduleCalculationRule = EXISTING_PRISONER,
      )
      createInductionScheduleHistory(
        reference = inductionScheduleReference,
        prisonNumber = prisonNumber,
        status = inductionScheduleStatus,
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
            it.wasStatus(InductionScheduleStatusApi.valueOf(inductionScheduleStatus.toString()))
              .hasDeadlineDate(originalInductionDueDate)
              .wasCreatedAtPrison("BXI")
              .wasUpdatedAtPrison("BXI")
          }
          .inductionScheduleVersion(2) {
            it.wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
              .hasDeadlineDate(TODAY)
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

    // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction Schedule but it was never scheduled because it was still waiting for Curious S&As
    @Test
    fun `should not re-schedule the Induction Schedule given prisoner that already has a PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS Induction Schedule`() {
      // Given
      // an induction schedule is created
      val prisonNumber = randomValidPrisonNumber()
      val originalInductionDueDate = TODAY.minusWeeks(10)
      val inductionScheduleReference = UUID.randomUUID()
      createInductionSchedule(
        reference = inductionScheduleReference,
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
        deadlineDate = originalInductionDueDate,
      )
      createInductionScheduleHistory(
        reference = inductionScheduleReference,
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
        deadlineDate = originalInductionDueDate,
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
        val inductionSchedules = getInductionScheduleHistory(prisonNumber)
        assertThat(inductionSchedules)
          .hasNumberOfInductionScheduleVersions(1)
          .inductionScheduleVersion(1) {
            it.wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
              .hasDeadlineDate(originalInductionDueDate)
              .wasVersion(1)
          }

        // test that no outbound events were created
        assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)
      }
    }

    // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction Schedule but it was never scheduled because it was still waiting for Curious S&As. Their release message was never processed (so the status is not EXEMPT_RELEASE), and they have since had their S&A's completed (race condition; S&A message is processed before prisoner admission)
    @Test
    fun `should re-schedule the Induction Schedule given prisoner that already has a PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS Induction Schedule but also has their screenings and assessments completed`() {
      // Given
      // an induction schedule is created
      val prisonNumber = randomValidPrisonNumber()
      val originalInductionDueDate = TODAY.minusWeeks(10)
      val inductionScheduleReference = UUID.randomUUID()
      createInductionSchedule(
        reference = inductionScheduleReference,
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
        deadlineDate = originalInductionDueDate,
      )
      createInductionScheduleHistory(
        reference = inductionScheduleReference,
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
        deadlineDate = originalInductionDueDate,
      )

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

      val expectedInductionDueDate = TODAY.plusDays(10)

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
            it.wasStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS)
              .hasDeadlineDate(originalInductionDueDate)
              .wasCreatedAtPrison("BXI")
              .wasUpdatedAtPrison("BXI")
          }
          .inductionScheduleVersion(2) {
            it.wasStatus(SCHEDULED)
              .hasDeadlineDate(expectedInductionDueDate)
              .wasCreatedAtPrison("BXI")
              .wasUpdatedAtPrison("BXI")
          }

        val inductionSchedule = getInductionSchedule(prisonNumber)
        assertThat(inductionSchedule).wasScheduleCalculationRule(InductionScheduleCalculationRule.NEW_PRISON_ADMISSION)

        // test that outbound events are also created:
        val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
        assertThat(inductionScheduleEvent)
          .hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
          .hasNumberOfPersonReferenceIdentifiers(1)
          .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
      }
    }

    // Re-offending prisoner. Prisoner previously had a PLP Induction + Goals, and has an InductionSchedule in a state other than COMPLETED. (this is a weird edge case)
    @Test
    fun `should update Induction Schedule and create Review Schedule given prisoner that already has a PLP Induction and Goals, and an Induction Schedule that is not COMPLETED`() {
      // Given
      // prisoner has a PLP Action Plan (Induction + at least 1 Goal), and an Induction Schedule that is not COMPLETED
      val prisonNumber = randomValidPrisonNumber()
      with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
        createPrisonerAPIStub(prisonNumber, this)
      }

      // Create the induction schedule, induction and action plan. This will have created the initial Review Schedule which for the purpose of this test we will need to delete
      createInductionSchedule(
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.SCHEDULED,
        createdAtPrison = "BXI",
      )
      createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = "BXI"))
      createActionPlan(prisonNumber)
      updateInductionScheduleRecordStatus(prisonNumber, InductionScheduleStatus.SCHEDULED)

      // The above calls set the data up but they will also generate events so clear these out before starting the test.
      // Even though this test is simulating prisoners with an Action Plan (Induction + Goal(s)) but without an Induction or Review Schedule (ie. a prisoner from before Reviews),
      // the above calls will have created the initial ReviewSchedule
      // Before clearing the queues though we need to wait until the first "plp.review-schedule.updated" event on the REVIEW queue is received.
      await untilCallTo {
        reviewScheduleEventQueue.countAllMessagesOnQueue()
      } matches { it != null && it > 0 }
      clearQueues()
      reviewScheduleRepository.deleteAll()
      reviewScheduleHistoryRepository.deleteAll()

      assertThat(getInductionScheduleHistory(prisonNumber)).hasNumberOfInductionScheduleVersions(1)
      assertThat(getReviewSchedules(prisonNumber)).hasNumberOfReviewSchedules(0)

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
        // test induction schedule was updated
        val inductionSchedule = getInductionSchedule(prisonNumber)
        assertThat(inductionSchedule)
          .wasStatus(COMPLETED)

        // test induction schedule history was updated
        val inductionScheduleHistories = getInductionScheduleHistory(prisonNumber)
        assertThat(inductionScheduleHistories)
          .hasNumberOfInductionScheduleVersions(2)

        // test that outbound event is also created:
        val inductionScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.INDUCTION)
        assertThat(inductionScheduleEvent)
          .hasDetailUrl("http://localhost:8080/inductions/$prisonNumber/induction-schedule")
          .hasNumberOfPersonReferenceIdentifiers(1)
          .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }

        // assert that there is a correctly setup ReviewSchedule
        assertThat(getReviewSchedules(prisonNumber))
          .hasNumberOfReviewSchedules(1)
          .reviewScheduleAtIndex(1) {
            it.hasStatus(ReviewScheduleStatus.SCHEDULED)
              .hasCalculationRule(ReviewScheduleCalculationRule.PRISONER_READMISSION)
          }

        // test that outbound event is also created:
        val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.REVIEW)
        assertThat(reviewScheduleEvent)
          .hasDetailUrl("http://localhost:8080/reviews/$prisonNumber/review-schedule")
          .hasNumberOfPersonReferenceIdentifiers(1)
          .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
      }
    }
  }

  @Nested
  @DisplayName("Tests for when the prisoner is admitted/received into prison, where we expect the Review Schedule to be created or updated")
  inner class ReviewSchedule {
    // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction that was completed and Review Schedule that is not active
    @ParameterizedTest
    @CsvSource(value = ["COMPLETED", "EXEMPT_PRISONER_DEATH", "EXEMPT_PRISONER_RELEASE", "EXEMPT_PRISONER_RELEASE_HOSPITAL"])
    fun `should create a new Review Schedule given prisoner that already has a completed Induction and Review Schedule in status`(reviewScheduleStatus: ReviewScheduleStatusEntity) {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI")) {
        createPrisonerAPIStub(prisonNumber, this)
      }

      // Create the induction schedule, induction and action plan. This will have created the initial Review Schedule
      createInductionSchedule(
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.COMPLETED,
        createdAtPrison = "BXI",
      )
      createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = "BXI"))
      createActionPlan(prisonNumber)
      updateReviewScheduleRecordStatus(
        prisonNumber = prisonNumber,
        status = reviewScheduleStatus,
      )

      // The above calls set the data up but they will also generate events so clear these out before starting the test.
      // Before clearing the queues though we need to wait until the first "plp.review-schedule.updated" event on the REVIEW queue is received.
      await untilCallTo {
        reviewScheduleEventQueue.countAllMessagesOnQueue()
      } matches { it != null && it > 0 }
      clearQueues()

      with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
        createPrisonerAPIStub(prisonNumber, this)
      }

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
        val actionPlanReviews = getActionPlanReviews(prisonNumber)
        assertThat(actionPlanReviews)
          .latestReviewSchedule {
            it.hasStatus(ReviewScheduleStatus.SCHEDULED)
              .hasCalculationRule(ReviewScheduleCalculationRule.PRISONER_READMISSION)
              .hasReviewType(ReviewType.TRANSFER_REVIEW)
              .wasCreatedAtOrAfter(earliestDateTime)
              .wasUpdatedAtOrAfter(earliestDateTime)
              .wasCreatedAtPrison("MDI")
              .wasUpdatedAtPrison("MDI")
          }

        // test that outbound event is also created:
        val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.REVIEW)
        assertThat(reviewScheduleEvent)
          .hasDetailUrl("http://localhost:8080/reviews/$prisonNumber/review-schedule")
          .hasNumberOfPersonReferenceIdentifiers(1)
          .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
      }
    }

    // Re-offending prisoner, re-admitted to prison. Prisoner previously had a PLP Induction that was completed but Review Schedule that is still active
    // Weird edge case scenario as we would expect a previously released/transferred prisoner who is being re-admitted to have had their Review Schedules exempted due to release or transfer via the prisoner.release listener
    // This scenario is to cover the edge case that the Review Schedule did not get exempted for some reason when the prisoner was released, and was left "hanging"
    // In this scenario the Review Schedule is marked as EXEMPT_UNKNOWN and then a new Review Schedule is created.
    @ParameterizedTest
    @CsvSource(value = ["SCHEDULED", "EXEMPT_PRISONER_FAILED_TO_ENGAGE"]) // All exemptions except EXEMPT_PRISONER_DEATH and EXEMPT_PRISONER_RELEASE fall into this category; only testing 1 here
    fun `should re-schedule active Review Schedule given prisoner that already has a completed Induction and Review Schedule in status`(reviewScheduleStatus: ReviewScheduleStatusEntity) {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "BXI")) {
        createPrisonerAPIStub(prisonNumber, this)
      }

      // Create the induction schedule, induction and action plan. This will have created the initial Review Schedule
      createInductionSchedule(
        prisonNumber = prisonNumber,
        status = InductionScheduleStatus.COMPLETED,
        createdAtPrison = "BXI",
      )
      createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork(prisonId = "BXI"))
      createActionPlan(prisonNumber)
      updateReviewScheduleRecordStatus(
        prisonNumber = prisonNumber,
        status = reviewScheduleStatus,
      )

      // The above calls set the data up but they will also generate events so clear these out before starting the test.
      // Before clearing the queues though we need to wait until the first "plp.review-schedule.updated" event on the REVIEW queue is received.
      await untilCallTo {
        reviewScheduleEventQueue.countAllMessagesOnQueue()
      } matches { it != null && it > 0 }
      clearQueues()

      with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
        createPrisonerAPIStub(prisonNumber, this)
      }

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
        val reviewSchedules = getReviewSchedules(prisonNumber)
        // Expect 3 Review Schedule versions - the original one, the exempted one, and the new one
        val newReviewScheduleReference = reviewSchedules.reviewSchedules[0].reference
        val originalReviewScheduleReference = reviewSchedules.reviewSchedules[1].reference
        assertThat(reviewSchedules)
          .hasNumberOfReviewSchedules(3)
          // The first in the list should be the newly created and scheduled Review Schedule - it is version 1 (because it is a new Review Schedule)
          .reviewScheduleAtIndex(1) {
            it.hasStatus(ReviewScheduleStatus.SCHEDULED)
              .hasCalculationRule(ReviewScheduleCalculationRule.PRISONER_READMISSION)
              .wasCreatedAtPrison("MDI")
              .wasUpdatedAtPrison("MDI")
              .isVersion(1)
              .hasReference(newReviewScheduleReference)
          }
          // The next one is the exempted Review Schedule - it is version 2
          .reviewScheduleAtIndex(2) {
            it.hasStatus(ReviewScheduleStatus.EXEMPT_UNKNOWN)
              .hasCalculationRule(ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE)
              .wasCreatedAtPrison("BXI")
              .wasUpdatedAtPrison("MDI")
              .isVersion(2)
              .hasReference(originalReviewScheduleReference)
          }
          // And the last in the list should be the original scheduled Review Schedule - it is version 1
          // The originally scheduled Review Schedule and the exempted Review Schedule should have the same reference because they represent the same Review Schedule
          .reviewScheduleAtIndex(3) {
            it.hasStatus(ReviewScheduleStatus.SCHEDULED)
              .hasCalculationRule(ReviewScheduleCalculationRule.BETWEEN_6_AND_12_MONTHS_TO_SERVE)
              .wasCreatedAtPrison("BXI")
              .wasUpdatedAtPrison("BXI")
              .isVersion(1)
              .hasReference(originalReviewScheduleReference)
          }
        assertThat(newReviewScheduleReference).isNotEqualTo(originalReviewScheduleReference)

        // test that outbound events are also created (there would have been 3 in total, but we cleared the queue after the first one in the given block above). The 2 new ones are the ones we are really interested in though)
        val reviewScheduleEvents = inductionScheduleEventQueue.receiveEventsOnQueue(QueueType.REVIEW)
        assertThat(reviewScheduleEvents)
          .hasNumberOfEvents(2)
          .allEvents {
            it.hasDetailUrl("http://localhost:8080/reviews/$prisonNumber/review-schedule")
              .hasNumberOfPersonReferenceIdentifiers(1)
              .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
          }
      }
    }

    // Re-offending prisoner. Prisoner previously had a PLP Induction + Goals from long before the Reviews process, and therefore does not have an Induction Schedule or Review Schedule in any state.
    @Test
    fun `should create Review Schedule instead of Induction Schedule given prisoner that already has a PLP Induction and Goals but does not have an Induction Schedule or Review Schedule`() {
      // Given
      // prisoner has a PLP Action Plan (Induction + at least 1 Goal), but no Induction Schedule or Review Schedules
      val prisonNumber = randomValidPrisonNumber()

      with(aValidPrisoner(prisonerNumber = prisonNumber, prisonId = "MDI")) {
        createPrisonerAPIStub(prisonNumber, this)
      }

      createInduction(prisonNumber, aValidCreateInductionRequest())
      createActionPlan(prisonNumber)

      // The above calls set the data up but they will also generate events so clear these out before starting the test.
      // Even though this test is simulating prisoners with an Action Plan (Induction + Goal(s)) but without an Induction or Review Schedule (ie. a prisoner from before Reviews),
      // the above calls will have created the initial ReviewSchedule
      // Before clearing the queues though we need to wait until the first "plp.review-schedule.updated" event on the REVIEW queue is received.
      await untilCallTo {
        reviewScheduleEventQueue.countAllMessagesOnQueue()
      } matches { it != null && it > 0 }
      clearQueues()
      reviewScheduleRepository.deleteAll()
      reviewScheduleHistoryRepository.deleteAll()

      assertThat(getInductionScheduleHistory(prisonNumber)).hasNumberOfInductionScheduleVersions(0)
      assertThat(getReviewSchedules(prisonNumber)).hasNumberOfReviewSchedules(0)

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
        // assert that no induction schedules exist or were created for this prisoner, and that no outbound induction events were created
        assertThat(getInductionScheduleHistory(prisonNumber)).hasNumberOfInductionScheduleVersions(0)
        assertThat(inductionScheduleEventQueue.countAllMessagesOnQueue()).isEqualTo(0)

        // assert that there is a correctly setup ReviewSchedule
        assertThat(getReviewSchedules(prisonNumber))
          .hasNumberOfReviewSchedules(1)
          .reviewScheduleAtIndex(1) {
            it.hasStatus(ReviewScheduleStatus.SCHEDULED)
              .hasCalculationRule(ReviewScheduleCalculationRule.PRISONER_READMISSION)
          }

        // test that outbound event is also created:
        val reviewScheduleEvent = inductionScheduleEventQueue.receiveEvent(QueueType.REVIEW)
        assertThat(reviewScheduleEvent)
          .hasDetailUrl("http://localhost:8080/reviews/$prisonNumber/review-schedule")
          .hasNumberOfPersonReferenceIdentifiers(1)
          .personReferenceIdentifier(1) { it.hasValue(prisonNumber) }
      }
    }
  }
}
