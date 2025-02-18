package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerMergedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_MERGED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RELEASED_FROM_PRISON

private val log = KotlinLogging.logger {}

/**
 * Service class that processes SQS events. For each type the [InboundEvent]'s `additionalInformation` property is
 * deserialized into it's corresponding type, and then the [InboundEvent] and it's [AdditionalInformation] are sent
 * to the relevant service specific to that event type.
 */
@Service
class InboundEventsService(
  private val mapper: ObjectMapper,
  private val prisonerReceivedIntoPrisonEventService: PrisonerReceivedIntoPrisonEventService,
  private val prisonerReleasedFromPrisonEventService: PrisonerReleasedFromPrisonEventService,
  private val prisonerMergedEventService: PrisonerMergedEventService,
) {

  fun process(inboundEvent: InboundEvent) = with(inboundEvent) {
    log.info { "Processing inbound event $eventType" }

    when (eventType) {
      PRISONER_RECEIVED_INTO_PRISON -> {
        val additionalInformation = eventAdditionalInformation<PrisonerReceivedAdditionalInformation>(inboundEvent)
        prisonerReceivedIntoPrisonEventService.process(inboundEvent, additionalInformation)
      }
      PRISONER_RELEASED_FROM_PRISON -> {
        val additionalInformation = eventAdditionalInformation<PrisonerReleasedAdditionalInformation>(inboundEvent)
        prisonerReleasedFromPrisonEventService.process(inboundEvent, additionalInformation)
      }
      PRISONER_MERGED -> {
        val additionalInformation = eventAdditionalInformation<PrisonerMergedAdditionalInformation>(inboundEvent)
        prisonerMergedEventService.process(inboundEvent, additionalInformation)
      }
    }
  }

  private inline fun <reified T : AdditionalInformation> eventAdditionalInformation(inboundEvent: InboundEvent): T = this.mapper.readValue(inboundEvent.additionalInformation, T::class.java)
}
