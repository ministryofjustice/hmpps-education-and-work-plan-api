package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.aValidPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus as InductionScheduleStatusResponse

/**
 * Integration tests for prison-admission handling under the PES contract (`ciag-kpi-processing-rule=PES`), where a
 * prisoner who already has an Induction Schedule pending their Curious Screening & Assessments is scheduled on
 * admission if their S&As have since been completed.
 */
@Isolated
@TestPropertySource(properties = ["ciag-kpi-processing-rule=PES"])
class PrisonerReceivedDueToAdmissionEventPesTest : IntegrationTestBase() {

  @BeforeEach
  fun setUp() {
    educationAssessmentEventRepository.deleteAll()
  }

  @Test
  fun `should schedule pending induction on admission given the prisoner's assessments are complete`() {
    // Given
    val prisonNumber = randomValidPrisonNumber()
    val inductionScheduleReference = UUID.randomUUID()

    createInductionSchedule(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatusEntity.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      deadlineDate = LocalDate.now(),
      createdAtPrison = "BXI",
    )
    createInductionScheduleHistory(
      reference = inductionScheduleReference,
      prisonNumber = prisonNumber,
      status = InductionScheduleStatusEntity.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS,
      deadlineDate = LocalDate.now(),
      createdAtPrison = "BXI",
    )

    // The prisoner's Screening & Assessments have been completed in Curious
    educationAssessmentEventRepository.save(
      EducationAssessmentEventEntity(
        reference = UUID.randomUUID(),
        prisonNumber = prisonNumber,
        status = EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now().minusDays(2),
        source = "CURIOUS",
        detailUrl = null,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
      ),
    )

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
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilAsserted {
      val inductionSchedule = getInductionSchedule(prisonNumber)
      assertThat(inductionSchedule)
        .wasStatus(InductionScheduleStatusResponse.SCHEDULED)
        .hasDeadlineDate(LocalDate.now().plusDays(10))
    }
  }
}
