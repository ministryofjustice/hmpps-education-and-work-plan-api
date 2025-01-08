package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.CiagKpiService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.RETURN_FROM_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import java.time.Instant

private val log = KotlinLogging.logger {}

@Service
class PrisonerReceivedIntoPrisonEventService(
  // ciagKpiService is nullable because the bean is dependent on the property `ciag-kpi-processing-rule` - when the property is not set this feature/functionality is disabled
  private val ciagKpiService: CiagKpiService?,
  private val reviewScheduleService: ReviewScheduleService,
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

    ciagKpiService?.processPrisonerAdmission(
      prisonNumber = nomsNumber,
      prisonAdmittedTo = prisonId,
      eventDate = eventOccurredAt,
    )

    /*
    TODO - replace the above call to the CiagKpiService with the following logic / calls to the InductionScheduleService and ReviewScheduleService
       - If prisoner has no Induction, Action Plan or Induction Schedule, then create the Induction Schedule
       - If prisoner has an Induction Schedule that is complete, create or update their Review Schedule with status SCHEDULED, reason PRISONER_READMISSION and due date +10 days
       - if prisoner has an Induction Schedule, and it is not complete, reschedule it with the same deadline date rules as if it were a new admission (even if it was exempt (exempt thru transfers handled below))
     (check all of the above rules with Aliki)
     */
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
}
