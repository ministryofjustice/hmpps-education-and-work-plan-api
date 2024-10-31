package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.RETURN_FROM_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED

private val log = KotlinLogging.logger {}

@Service
class PrisonerReceivedIntoPrisonEventService(private val inductionService: InductionService) {
  fun process(inboundEvent: InboundEvent, additionalInformation: PrisonerReceivedAdditionalInformation) {
    log.info { "Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
    when (additionalInformation.reason) {
      ADMISSION, TRANSFERRED -> {
        inductionService.createOrUpdateInductionSchedule(
          prisonNumber = inboundEvent.prisonNumber(),
          eventDate = inboundEvent.occurredAt,
        )
      }

      TEMPORARY_ABSENCE_RETURN, RETURN_FROM_COURT -> {
        log.debug { "Ignoring Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
      }
    }
  }
}
