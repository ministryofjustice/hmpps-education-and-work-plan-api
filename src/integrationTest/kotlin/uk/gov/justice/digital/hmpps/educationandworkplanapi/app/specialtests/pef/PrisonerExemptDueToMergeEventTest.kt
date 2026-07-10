package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.specialtests.pef

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.SqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidHmppsDomainEventsSqsMessage
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.aValidPrisonerMergedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.util.UUID

@Isolated
@ActiveProfiles("integration-test", "ciag-kpi-pef-rules")
class PrisonerExemptDueToMergeEventTest : IntegrationTestBase() {
  @Test
  fun `should update Review Schedule given merged prisoner had an active Review Schedule and new PRN does not completed Screenings and Assessments`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())
    createActionPlan(prisonNumber)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    val sqsMessage = prisonerMergedSqsMessage(prisonNumber)

    val rootNode = objectMapper.readTree(sqsMessage.Message)
    val newNomisNumber = rootNode["additionalInformation"]["nomsNumber"].asText()

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val actionPlanReviews = getActionPlanReviews(prisonNumber)
    assertThat(actionPlanReviews)
      .latestReviewSchedule {
        it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_MERGE)
      }

    // assert that the other prison number is processed as a new admission with the correct induction schedule status
    assertThat(inductionScheduleRepository.findByPrisonNumber(newNomisNumber))
      .hasScheduleStatus(InductionScheduleStatus.SCHEDULED) // PEF rules will set the initial status to SCHEDULED
      .hasDeadlineDate(
        LocalDate.now().plusDays(20),
      ) // Deadline date will have been set to today + 20 days as per PEF rules
  }

  @Test
  fun `should update Review Schedule given merged prisoner had an active Review Schedule and new PRN has completed Screenings and Assessments`() {
    // Given
    // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
    val prisonNumber = setUpRandomPrisoner()
    createInduction(prisonNumber, aValidCreateInductionRequestForPrisonerNotLookingToWork())
    createActionPlan(prisonNumber)

    // The above calls set the data up but they will also generate events so clear these out before starting the test.
    // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
    await untilCallTo {
      reviewScheduleEventQueue.countAllMessagesOnQueue()
    } matches { it != null && it > 0 }
    clearQueues()

    val sqsMessage = prisonerMergedSqsMessage(prisonNumber)

    val rootNode = objectMapper.readTree(sqsMessage.Message)
    val newNomisNumber = rootNode["additionalInformation"]["nomsNumber"].asText()

    educationAssessmentEventRepository.save(
      EducationAssessmentEventEntity(
        reference = UUID.randomUUID(),
        prisonNumber = newNomisNumber,
        status = ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now().minusDays(2),
        source = "CURIOUS",
        detailUrl = null,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
      ),
    )

    // When
    sendDomainEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    val actionPlanReviews = getActionPlanReviews(prisonNumber)
    assertThat(actionPlanReviews)
      .latestReviewSchedule {
        it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_MERGE)
      }

    // assert that the other prison number is processed as a new admission with the correct induction schedule status
    assertThat(inductionScheduleRepository.findByPrisonNumber(newNomisNumber))
      .hasScheduleStatus(InductionScheduleStatus.SCHEDULED) // PEF rules will set the initial status to SCHEDULED
      .hasDeadlineDate(
        LocalDate.now().plusDays(20),
      ) // Deadline date will have been set to today + 20 days as per PEF rules
  }

  private fun prisonerMergedSqsMessage(prisonNumber: String): SqsMessage {
    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prisonNumber,
      eventType = EventType.PRISONER_MERGED,
      additionalInformation = aValidPrisonerMergedAdditionalInformation(
        removedNomsNumber = prisonNumber,
        reason = AdditionalInformation.PrisonerMergedAdditionalInformation.Reason.MERGE,
      ),
    )
    return sqsMessage
  }
}
