package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.COMPLETED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.RETURN_FROM_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.review.CreateInitialReviewScheduleMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
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
) {
  fun process(inboundEvent: InboundEvent, additionalInformation: PrisonerReceivedAdditionalInformation) = with(additionalInformation) {
    when (reason) {
      ADMISSION -> processPrisonerAdmissionEvent(inboundEvent.occurredAt)

      TRANSFERRED -> processPrisonerTransferEvent()

      TEMPORARY_ABSENCE_RETURN, RETURN_FROM_COURT ->
        log.debug { "Ignoring Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
    }
  }

  private fun PrisonerReceivedAdditionalInformation.processPrisonerAdmissionEvent(eventOccurredAt: Instant) {
    log.info { "Processing Prisoner Admission Event for prisoner [$nomsNumber]" }

    val prisoner = prisonerSearchApiService.getPrisoner(nomsNumber)
    val prisonId = prisoner.prisonId ?: "N/A"
    val prisonerAdmissionDate = LocalDate.ofInstant(eventOccurredAt, ZoneOffset.UTC)
    try {
      // Attempt to create the prisoner's Induction Schedule
      inductionScheduleService.createInductionSchedule(
        prisonNumber = nomsNumber,
        prisonerAdmissionDate = prisonerAdmissionDate,
        prisonId = prisonId,
        releaseDate = prisoner.releaseDate,
      )
    } catch (e: InductionScheduleAlreadyExistsException) {
      // Prisoner already has an Induction Schedule
      when (e.inductionSchedule.scheduleStatus) {
        COMPLETED -> {
          // The Induction was completed so we need to reschedule their active Review Schedule if they have one, or create a new Review Schedule.
          rescheduleOrCreatePrisonersReviewSchedule(prisoner, prisonId)
        }

        else -> {
          // The Induction was not completed so need to reschedule it with a new deadline date.
          inductionScheduleService.reschedulePrisonersInductionSchedule(
            nomsNumber,
            prisonerAdmissionDate = prisonerAdmissionDate,
            prisonId = prisonId,
            releaseDate = prisoner.releaseDate,
          )
        }
      }
    }
  }

  private fun PrisonerReceivedAdditionalInformation.processPrisonerTransferEvent() {
    log.info { "Processing Prisoner Transfer Event for prisoner [$nomsNumber] at prison [$prisonId]" }

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

  private fun rescheduleOrCreatePrisonersReviewSchedule(prisoner: Prisoner, prisonId: String) {
    val reviewSchedule =
      runCatching { reviewScheduleService.getActiveReviewScheduleForPrisoner(prisoner.prisonerNumber) }.getOrNull()
    if (reviewSchedule != null) {
      // An active Review Schedule exists - Exempt it for unknown reason, which will make it non-active
      reviewScheduleService.exemptActiveReviewScheduleStatusDueToUnknownReason(
        prisonNumber = prisoner.prisonerNumber,
        prisonId = prisonId,
      )
    }

    // Create a new Review Schedule for the prisoner
    val reviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
      prisoner = prisoner,
      // If the prisoner is being admitted (prisoner.admission event) and they already have an Induction Schedule, this MUST be a re-admission (re-offender)
      isReadmission = true,
      isTransfer = false,
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
}
