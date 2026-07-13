package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_MERGED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.aValidCreateInductionRequestForPrisonerNotLookingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InductionScheduleStatus as InductionScheduleStatusEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus as ReviewScheduleStatusEntity

@Isolated
class PrisonerExemptDueToMergeEventTest : IntegrationTestBase() {

  @Nested
  inner class PrisonersMergedTheRightWayRound {
    // Tests for prisoner merge events where the prisoner being merged away is the new PRN from the recent admission
    // and the prisoner being kept is the original prisoner with some history of LWP data
    // This is considered the "right way round" because in most cases you would want to keep the original/older PRN
    // with the historic LWP data.

    @Test
    fun `should not update Review Schedule given merged prisoner does not have an active Review Schedule`() {
      // Given
      val prnToRemove = setUpRandomPrisoner()

      val prnToKeep = setUpRandomPrisoner()
      // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
      createInduction(prnToRemove, aValidCreateInductionRequestForPrisonerNotLookingToWork())
      createActionPlan(prnToRemove)

      // Before updating the status of the Review Schedule we need to wait until 1 "plp.review-schedule.updated" event on the REVIEW queue is received (which is the creation of the initial Review Schedule)
      await untilCallTo {
        reviewScheduleEventQueue.countAllMessagesOnQueue()
      } matches { it != null && it > 0 }
      // Update the status of the Review Schedule to Completed, which means the prisoner no longer has an active Review Schedule
      updateReviewScheduleRecordStatus(prnToRemove, ReviewScheduleStatusEntity.COMPLETED)

      clearQueues()

      val sqsMessage = prisonerMergedSqsMessage(prnToRemove, prnToKeep)

      // When
      sendDomainEvent(sqsMessage)

      // Then
      // wait until the queue is drained / message is processed
      await untilCallTo {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      } matches { it == 0 }

      val actionPlanReviews = getActionPlanReviews(prnToRemove)
      assertThat(actionPlanReviews)
        .latestReviewSchedule {
          it.hasStatus(ReviewScheduleStatus.COMPLETED)
        }
    }

    @Test
    fun `should not create or update Review Schedule given merged prisoner does not have a Review Schedule at all`() {
      // Given
      val prnToRemove = randomValidPrisonNumber()

      val prnToKeep = randomValidPrisonNumber()
      assertThat(runCatching { getActionPlanReviews(prnToKeep) }.getOrNull()).isNull()

      val sqsMessage = prisonerMergedSqsMessage(prnToRemove, prnToKeep)

      // When
      sendDomainEvent(sqsMessage)

      // Then
      // wait until the queue is drained / message is processed
      await untilCallTo {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      } matches { it == 0 }

      assertThat(runCatching { getActionPlanReviews(prnToKeep) }.getOrNull()).isNull()
    }

    @Test
    fun `should not update Induction Schedule given merged prisoner had a completed Induction Schedule`() {
      // Given
      val prnToRemove = setUpRandomPrisoner()

      val prnToKeep = randomValidPrisonNumber()
      createInductionSchedule(prnToKeep, status = InductionScheduleStatusEntity.COMPLETED)

      val sqsMessage = prisonerMergedSqsMessage(prnToRemove, prnToKeep)

      // When
      sendDomainEvent(sqsMessage)

      // Then
      // wait until the queue is drained / message is processed
      await untilCallTo {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      } matches { it == 0 }

      await untilAsserted {
        val inductionSchedule = getInductionSchedule(prnToKeep)
        assertThat(inductionSchedule)
          .wasStatus(InductionScheduleStatus.COMPLETED)
      }
    }
  }

  @Nested
  inner class PrisonersMergedTheWrongWayRound {
    // Tests for prisoner merge events where the prisoner being merged away is the prisoner with some history of LWP data
    // and the prisoner being kept is the new PRN from the recent admission
    // This can happen in NOMIS, but is rare, and is considered the "wrong way round" because the newer PRN (most recent admission)
    // is usually the one that should be merged away

    @Test
    fun `should update Review Schedule given merged prisoner had an active Review Schedule and new PRN does not have completed Screenings and Assessments`() {
      // Given
      // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
      val prnToRemove = setUpRandomPrisoner()
      createInduction(prnToRemove, aValidCreateInductionRequestForPrisonerNotLookingToWork())
      createActionPlan(prnToRemove)

      val prnToKeep = setUpRandomPrisoner()

      // The above calls set the data up but they will also generate events so clear these out before starting the test.
      // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
      await untilCallTo {
        reviewScheduleEventQueue.countAllMessagesOnQueue()
      } matches { it != null && it > 0 }
      clearQueues()

      val sqsMessage = prisonerMergedSqsMessage(prnToRemove, prnToKeep)

      // When
      sendDomainEvent(sqsMessage)

      // Then
      // wait until the queue is drained / message is processed
      await untilCallTo {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      } matches { it == 0 }

      val actionPlanReviews = getActionPlanReviews(prnToRemove)
      assertThat(actionPlanReviews)
        .latestReviewSchedule {
          it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_MERGE)
        }

      // assert that the other prison number is processed as a new admission with the correct induction schedule status
      assertThat(inductionScheduleRepository.findByPrisonNumber(prnToKeep))
        .hasScheduleStatus(PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS) // PES rules will set the initial status to PENDING_INITIAL_SCREENING_AND_ASSESSMENTS_FROM_CURIOUS
        .hasDeadlineDate(LocalDate.now()) // Deadline will have been set to "today", and will be rescheduled when the prisoner has their S&As completed
    }

    @Test
    fun `should update Review Schedule given merged prisoner had an active Review Schedule and new PRN has completed Screenings and Assessments`() {
      // Given
      // an induction and action plan are created. This will have created the initial Review Schedule with the status SCHEDULED
      val prnToRemove = setUpRandomPrisoner()
      createInduction(prnToRemove, aValidCreateInductionRequestForPrisonerNotLookingToWork())
      createActionPlan(prnToRemove)

      val prnToKeep = setUpRandomPrisoner()

      // The above calls set the data up but they will also generate events so clear these out before starting the test.
      // Before clearing the queues though we need to wait until the "plp.review-schedule.updated" event on the REVIEW queue is received.
      await untilCallTo {
        reviewScheduleEventQueue.countAllMessagesOnQueue()
      } matches { it != null && it > 0 }
      clearQueues()

      val sqsMessage = prisonerMergedSqsMessage(prnToRemove, prnToKeep)

      educationAssessmentEventRepository.save(
        EducationAssessmentEventEntity(
          reference = UUID.randomUUID(),
          prisonNumber = prnToKeep,
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

      val actionPlanReviews = getActionPlanReviews(prnToRemove)
      assertThat(actionPlanReviews)
        .latestReviewSchedule {
          it.hasStatus(ReviewScheduleStatus.EXEMPT_PRISONER_MERGE)
        }

      // assert that the other prison number is processed as a new admission with the correct induction schedule status
      assertThat(inductionScheduleRepository.findByPrisonNumber(prnToKeep))
        .hasScheduleStatus(SCHEDULED) // PES rules will set the initial status to SCHEDULED because the victor in the merge event already has their S&As completed
        .hasDeadlineDate(LocalDate.now().plusDays(10)) // Deadline date will have been set to today + 10 days as per PES rules
    }

    @Test
    fun `should update Induction Schedule given merged prisoner had an active Induction Schedule`() {
      // Given
      val prnToRemove = randomValidPrisonNumber()
      createInductionSchedule(prnToRemove)

      val prnToKeep = setUpRandomPrisoner()

      val sqsMessage = prisonerMergedSqsMessage(prnToRemove, prnToKeep)

      // When
      sendDomainEvent(sqsMessage)

      // Then
      // wait until the queue is drained / message is processed
      await untilCallTo {
        domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
      } matches { it == 0 }

      await untilAsserted {
        val inductionSchedule = getInductionSchedule(prnToRemove)
        assertThat(inductionSchedule)
          .wasStatus(InductionScheduleStatus.EXEMPT_PRISONER_MERGE)
      }
    }
  }

  private fun prisonerMergedSqsMessage(prnToRemove: String, prnToKeep: String): SqsMessage {
    val sqsMessage = aValidHmppsDomainEventsSqsMessage(
      prisonNumber = prnToKeep,
      eventType = PRISONER_MERGED,
      additionalInformation = aValidPrisonerMergedAdditionalInformation(
        prisonNumber = prnToKeep,
        removedNomsNumber = prnToRemove,
        reason = AdditionalInformation.PrisonerMergedAdditionalInformation.Reason.MERGE,
      ),
    )
    return sqsMessage
  }
}
