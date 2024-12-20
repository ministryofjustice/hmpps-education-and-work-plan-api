package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.RELEASED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.RELEASED_TO_HOSPITAL
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.SENT_TO_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RELEASE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.TRANSFERRED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.UNKNOWN

private val log = KotlinLogging.logger {}

@Service
class PrisonerReleasedFromPrisonEventService(
  private val reviewScheduleService: ReviewScheduleService,
) {
  fun process(
    inboundEvent: InboundEvent,
    additionalInformation: PrisonerReleasedAdditionalInformation,
  ) =
    with(additionalInformation) {
      when (reason) {
        RELEASED -> {
          log.info { "Processing Prisoner Released From Prison Event with reason RELEASED for prisoner [$nomsNumber]" }
          try {
            reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerRelease(
              prisonNumber = nomsNumber,
              prisonId = prisonId,
            ).also {
              log.debug { "Review Schedule for prisoner [$nomsNumber] set to exempt: EXEMPT_PRISONER_RELEASE" }
            }
          } catch (e: ReviewScheduleNotFoundException) {
            log.debug { "Prisoner [$nomsNumber] does not have an active Review Schedule; no need to set it as exempt" }
          }
        }

        TEMPORARY_ABSENCE_RELEASE, RELEASED_TO_HOSPITAL, SENT_TO_COURT, TRANSFERRED, UNKNOWN -> {
          log.debug { "Ignoring Processing Prisoner Released From Prison Event with reason $reason" }
        }
      }
    }
}
