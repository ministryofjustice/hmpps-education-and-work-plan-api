package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
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

@Service
class PrisonerReceivedIntoPrisonEventService(
  private val inductionScheduleService: InductionScheduleService,
  private val reviewScheduleService: ReviewScheduleService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val createInitialReviewScheduleMapper: CreateInitialReviewScheduleMapper,
) {
  fun process(inboundEvent: InboundEvent, additionalInformation: PrisonerReceivedAdditionalInformation) =
    with(additionalInformation) {
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
    val prisonId = prisoner.prisonId ?: "NA"
    try {
      // Attempt to create the prisoner's Induction Schedule
      inductionScheduleService.createInductionSchedule(
        prisonNumber = nomsNumber,
        prisonerAdmissionDate = LocalDate.ofInstant(eventOccurredAt, ZoneOffset.UTC),
        prisonId = prisonId,
      )
    } catch (e: InductionScheduleAlreadyExistsException) {
      // Prisoner already has an Induction Schedule
      when (e.inductionSchedule.scheduleStatus) {
        COMPLETED -> {
          // The Induction was completed so we need to reschedule their active Review Schedule if they have one, or create a new Review Schedule.
          rescheduleOrCreatePrisonersReviewSchedule(nomsNumber, prisoner)
        }

        else -> {
          // The Induction was not completed so need to reschedule it with a new deadline date.
          // TODO - reschedule it
        }
      }
    }
  }

  private fun PrisonerReceivedAdditionalInformation.processPrisonerTransferEvent() {
    log.info { "Processing Prisoner Admission Event (due to transfer) for prisoner [$nomsNumber]" }

    try {
      reviewScheduleService.exemptAndReScheduleActiveReviewScheduleStatusDueToPrisonerTransfer(
        prisonNumber = nomsNumber,
        prisonTransferredTo = prisonId,
      )
    } catch (e: ReviewScheduleNotFoundException) {
      log.debug { "Prisoner [$nomsNumber] does not have an active Review Schedule; no need to exempt and re-schedule it" }
    }

    // TODO - RR-1215 - call inductionScheduleService to exempt & reschedule Induction Schedule due to prisoner transfer
  }

  private fun rescheduleOrCreatePrisonersReviewSchedule(prisonNumber: String, prisoner: Prisoner) {
    try {
      val reviewSchedule = reviewScheduleService.getActiveReviewScheduleForPrisoner(prisonNumber)
      // TODO - reschedule it
    } catch (e: ReviewScheduleNotFoundException) {
      // An active Review Schedule does not exist - create a new Review Schedule for the prisoner
      val reviewScheduleDto = createInitialReviewScheduleMapper.fromPrisonerToDomain(
        prisoner = prisoner,
        // If the prisoner is being admitted (prisoner.admission event) and they already have an Induction Schedule, this MUST be a re-admission (re-offender)
        isReadmission = true,
        isTransfer = false,
      )
      reviewScheduleService.createInitialReviewSchedule(reviewScheduleDto)
    }
  }
}
