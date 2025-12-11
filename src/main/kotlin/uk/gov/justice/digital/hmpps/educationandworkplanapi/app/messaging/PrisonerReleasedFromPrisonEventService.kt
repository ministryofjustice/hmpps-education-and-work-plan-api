package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.RELEASED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.RELEASED_TO_HOSPITAL
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.SENT_TO_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RELEASE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.UNKNOWN

private val log = KotlinLogging.logger {}

private const val REVIEW_SCHEDULE = "Review Schedule"
private const val INDUCTION_SCHEDULE = "Induction Schedule"

@Service
class PrisonerReleasedFromPrisonEventService(
  private val reviewScheduleService: ReviewScheduleService,
  private val inductionScheduleService: InductionScheduleService,
) {
  fun process(
    inboundEvent: InboundEvent,
    additionalInformation: PrisonerReleasedAdditionalInformation,
  ) = with(additionalInformation) {
    when (reason) {
      RELEASED ->
        if (releaseTriggeredByPrisonerDeath) processPrisonerReleaseEventDueToDeath() else processPrisonerReleaseEvent()
      RELEASED_TO_HOSPITAL -> processPrisonerReleaseEvent(releaseToHospital = true)

      TEMPORARY_ABSENCE_RELEASE,
      SENT_TO_COURT,
      UNKNOWN,
      // When a prisoner is transferred there are 2 events - a prisoner.released event for the "outbound" from the old prison, and a prisoner.received event for the "inbound" for the new prison
      // Given these events can come in close succession or potentially out of order, all processing of "transfer" events is handled once in the prisoner.received listener
      TRANSFERRED,
      -> log.debug { "Ignoring Processing Prisoner Released From Prison Event with reason $reason" }
    }
  }

  private fun PrisonerReleasedAdditionalInformation.processPrisonerReleaseEvent(releaseToHospital: Boolean = false) {
    log.info { "Processing Prisoner Released From Prison Event for prisoner [$nomsNumber]" }
    var inductionScheduleStatus = InductionScheduleStatus.EXEMPT_PRISONER_RELEASE
    var reviewScheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE
    if (releaseToHospital) {
      inductionScheduleStatus = InductionScheduleStatus.EXEMPT_PRISONER_RELEASE_HOSPITAL
      reviewScheduleStatus = ReviewScheduleStatus.EXEMPT_PRISONER_RELEASE_HOSPITAL
    }
    handle(
      REVIEW_SCHEDULE,
    ) { reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerRelease(nomsNumber, prisonId, reviewScheduleStatus) }
    handle(
      INDUCTION_SCHEDULE,
    ) { inductionScheduleService.exemptActiveInductionScheduleStatusDueToPrisonerRelease(nomsNumber, prisonId, inductionScheduleStatus) }
  }

  private fun PrisonerReleasedAdditionalInformation.processPrisonerReleaseEventDueToDeath() {
    log.info { "Processing Prisoner Released From Prison Event (due to death) for prisoner [$nomsNumber]" }
    handle(
      REVIEW_SCHEDULE,
    ) { reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerDeath(nomsNumber, prisonId) }
    handle(
      INDUCTION_SCHEDULE,
    ) { inductionScheduleService.exemptActiveInductionScheduleStatusDueToPrisonerDeath(nomsNumber, prisonId) }
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
