package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation

private val log = KotlinLogging.logger {}

@Service
class PrisonerReleasedFromPrisonEventService {
  fun process(
    inboundEvent: InboundEvent,
    additionalInformation: PrisonerReleasedAdditionalInformation,
  ) =
    log.info { "Processing Prisoner Released From Prison Event with reason ${additionalInformation.reason}" }
}
