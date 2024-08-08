package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.RETURN_FROM_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED

private val log = KotlinLogging.logger {}

@Service
class PrisonerReceivedIntoPrisonEventService {
  fun process(inboundEvent: InboundEvent, additionalInformation: PrisonerReceivedAdditionalInformation) =
    when (additionalInformation.reason) {
      ADMISSION -> {
        log.info { "Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
        // TODO - process prisoner admission event in respect of what PLP needs to do on this event
      }

      TRANSFERRED -> {
        log.info { "Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
        // TODO - process prisoner transfer event in respect of what PLP needs to do on this event
      }

      TEMPORARY_ABSENCE_RETURN, RETURN_FROM_COURT -> {
        log.debug { "Ignoring Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
      }
    }
}
