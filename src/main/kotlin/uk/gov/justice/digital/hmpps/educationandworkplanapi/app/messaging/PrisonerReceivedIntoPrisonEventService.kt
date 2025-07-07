package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule.NEW_PRISON_ADMISSION
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.RETURN_FROM_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ScheduleAdapter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val log = KotlinLogging.logger {}

private const val REVIEW_SCHEDULE = "Review Schedule"
private const val INDUCTION_SCHEDULE = "Induction Schedule"

@Service
class PrisonerReceivedIntoPrisonEventService(
  private val inductionScheduleService: InductionScheduleService,
  private val reviewScheduleService: ReviewScheduleService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
  private val inductionService: InductionService,
  private val actionPlanService: ActionPlanService,
  private val scheduleAdapter: ScheduleAdapter,
) {
  fun process(
    inboundEvent: InboundEvent,
    additionalInformation: PrisonerReceivedAdditionalInformation,
    dataCorrection: Boolean = false,
    treatAsTransfer: Boolean = false,
  ) = with(additionalInformation) {
    if (prisonId.length < 3) {
      log.error { "Ignoring inbound message for prisoner ${additionalInformation.nomsNumber} due to invalid prison id ($prisonId)" }
      return
    }

    when (reason) {
      ADMISSION -> processPrisonerAdmissionEvent(nomsNumber, inboundEvent.occurredAt, dataCorrection, treatAsTransfer)

      TRANSFERRED -> processPrisonerTransferEvent()

      TEMPORARY_ABSENCE_RETURN, RETURN_FROM_COURT ->
        log.debug { "Ignoring Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
    }
  }

  fun processPrisonerAdmissionEvent(
    nomsNumber: String,
    eventOccurredAt: Instant,
    dataCorrection: Boolean = false,
    treatAsTransfer: Boolean = false,
  ) {
    log.info { "Processing Prisoner Admission Event for prisoner [$nomsNumber]" }

    val prisoner = prisonerSearchApiService.getPrisoner(nomsNumber)
    val prisonId = prisoner.prisonId ?: "N/A"
    val prisonerAdmissionDate = LocalDate.ofInstant(eventOccurredAt, ZoneOffset.UTC)

    if (prisonerAlreadyHasActionPlan(nomsNumber)) {
      val inductionSchedule =
        runCatching { inductionScheduleService.getInductionScheduleForPrisoner(nomsNumber) }.getOrNull()

      if (inductionSchedule == null) {
        log.info { "Prisoner [$nomsNumber] already has an Action Plan but no InductionSchedule. Most likely a re-offender released before 01/04/25 and re-admitted after that date. Creating them a ReviewSchedule" }
        rescheduleOrCreatePrisonersReviewSchedule(prisoner, prisonId)
        return
      }

      if (inductionSchedule.scheduleStatus != COMPLETED) {
        log.info { "Prisoner [$nomsNumber] already has an Action Plan but their InductionSchedule is in a non-complete state. Setting their InductionSchedule to COMPLETED and creating them a ReviewSchedule" }
        inductionScheduleService.updateInductionSchedule(
          inductionSchedule = inductionSchedule,
          newStatus = COMPLETED,
          prisonId = prisonId,
        )
        rescheduleOrCreatePrisonersReviewSchedule(prisoner, prisonId, dataCorrection, treatAsTransfer)
        return
      }
    }

    try {
      // Attempt to create the prisoner's Induction Schedule
      inductionScheduleService.createInductionSchedule(
        prisonNumber = nomsNumber,
        prisonerAdmissionDate = prisonerAdmissionDate,
        prisonId = prisonId,
        releaseDate = prisoner.releaseDate,
        dataCorrection = dataCorrection,
      )
    } catch (e: InductionScheduleAlreadyExistsException) {
      // Prisoner already has an Induction Schedule
      when (e.inductionSchedule.scheduleStatus) {
        COMPLETED -> {
          // The Induction was completed so we need to reschedule their active Review Schedule if they have one, or create a new Review Schedule.
          rescheduleOrCreatePrisonersReviewSchedule(prisoner, prisonId)
        }

        else -> {
          // The Induction was not completed so need to reschedule it with a new deadline date
          // and update the calculation rule to be NEW_ADMISSION.
          inductionScheduleService.reschedulePrisonersInductionSchedule(
            nomsNumber,
            prisonerAdmissionDate = prisonerAdmissionDate,
            prisonId = prisonId,
            releaseDate = prisoner.releaseDate,
            calculationRule = NEW_PRISON_ADMISSION,
          )
        }
      }
    }
  }

  private fun PrisonerReceivedAdditionalInformation.processPrisonerTransferEvent() {
    log.info { "Processing Prisoner Transfer Event for prisoner [$nomsNumber] at prison [$prisonId]" }

    /**
     * It is possible that we can receive a transfer message without receiving a prisoner ADMISSION event.
     * if this happens and the person had an induction completed from a previous prison stay then they will not
     * have a review/induction schedule created and the prisoner will be stuck and require manual intervention
     * This block is belt and braces to ensure that the prisoner has the correct schedules set up.
     **/
    try {
      scheduleAdapter.completeInductionScheduleAndCreateInitialReviewSchedule(nomsNumber)
    } catch (e: ReviewScheduleNoReleaseDateForSentenceTypeException) {
      log.warn { "Exception thrown when completing induction or creating review schedule: ${e.message}" }
    }

    handle(
      scheduleType = REVIEW_SCHEDULE,
      action = {
        reviewScheduleService.exemptAndReScheduleActiveReviewScheduleDueToPrisonerTransfer(
          prisonNumber = nomsNumber,
          prisonTransferredTo = prisonId,
        )
      },
    )

    handle(
      scheduleType = INDUCTION_SCHEDULE,
      action = {
        inductionScheduleService.exemptAndReScheduleActiveInductionScheduleDueToPrisonerTransfer(
          prisonNumber = nomsNumber,
          prisonTransferredTo = prisonId,
        )
      },
    )
  }

  private fun rescheduleOrCreatePrisonersReviewSchedule(prisoner: Prisoner, prisonId: String, dataCorrection: Boolean = false, treatAsTransfer: Boolean = false) {
    val reviewSchedule =
      runCatching { reviewScheduleService.getActiveReviewScheduleForPrisoner(prisoner.prisonerNumber) }.getOrNull()
    if (reviewSchedule != null) {
      // An active Review Schedule exists - Exempt it for unknown reason, which will make it non-active
      reviewScheduleService.exemptActiveReviewScheduleStatusDueToUnknownReason(
        prisonNumber = prisoner.prisonerNumber,
        prisonId = prisonId,
      )
    }

    var readmission = true
    if (dataCorrection) {
      readmission = false
    }
    var transfer = false
    if (dataCorrection && treatAsTransfer) {
      transfer = true
    }

    // Create a new Review Schedule for the prisoner
    val reviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
      prisoner = prisoner,
      // If the prisoner is being admitted (prisoner.admission event) and they already have an Induction Schedule, this MUST be a re-admission (re-offender)
      isReadmission = readmission, // true
      isTransfer = transfer, // false
    )
    reviewScheduleService.createInitialReviewSchedule(reviewScheduleDto)
  }

  private fun handle(
    scheduleType: String,
    action: () -> Unit,
  ) {
    try {
      action()
    } catch (e: Exception) {
      if (e is ReviewScheduleNotFoundException || e is InductionScheduleNotFoundException) {
        log.debug { "Prisoner does not have an active $scheduleType; no need to set it as exempt" }
      } else {
        throw e // Re-throw unexpected exceptions
      }
    }
  }

  /**
   * Returns true if the prisoner already has an Action Plan (consisting of an Induction and at least 1 Goal), irrespective
   * of whether they have an InductionSchedule and/or ReviewSchedule or not
   */
  private fun prisonerAlreadyHasActionPlan(prisonNumber: String): Boolean = runCatching {
    inductionService.getInductionForPrisoner(prisonNumber)
  }.getOrNull() != null &&
    runCatching { actionPlanService.getActionPlan(prisonNumber) }.getOrNull() != null
}
