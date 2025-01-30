package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_MERGED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RECEIVED_INTO_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType.PRISONER_RELEASED_FROM_PRISON
import java.time.Instant
import java.util.UUID

fun aValidHmppsDomainEventsSqsMessage(
  prisonNumber: String = aValidPrisonNumber(),
  eventType: EventType = PRISONER_RECEIVED_INTO_PRISON,
  occurredAt: Instant = Instant.now().minusSeconds(10),
  publishedAt: Instant = Instant.now(),
  description: String = "A prisoner has been received into prison",
  version: String = "1.0",
  removedNomsNumber: String = "",
  additionalInformation: AdditionalInformation =
    when (eventType) {
      PRISONER_RECEIVED_INTO_PRISON -> aValidPrisonerReceivedAdditionalInformation(prisonNumber)
      PRISONER_RELEASED_FROM_PRISON -> aValidPrisonerReleasedAdditionalInformation(prisonNumber)
      PRISONER_MERGED -> aValidPrisonerMergedAdditionalInformation(prisonNumber, removedNomsNumber)
    },
): SqsMessage =
  SqsMessage(
    Type = "Notification",
    Message = """
        {
          "eventType": "${eventType.eventType}",
          "personReference": { "identifiers": [ { "type": "NOMS", "value": "$prisonNumber" } ] },
          "occurredAt": "$occurredAt",
          "publishedAt": "$publishedAt",
          "description": "$description",
          "version": "$version",
          "additionalInformation": ${ObjectMapper().writeValueAsString(additionalInformation)}
        }        
    """.trimIndent(),
    MessageId = UUID.randomUUID(),
  )
