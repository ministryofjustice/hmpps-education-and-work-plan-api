package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.annotation.JsonValue
import java.time.Instant

data class InboundEvent(
  val eventType: EventType,
  val personReference: PersonReference,
  val additionalInformation: AdditionalInformation,
) {
  fun eventType(): EventType = eventType
  fun reference() = additionalInformation.reference
  fun prisonNumber(): String = personReference.identifiers.first { it.type == "NOMS" }.value
  fun occurredAt() = additionalInformation.occurredAt
  fun prisonId() = additionalInformation.prisonId
  fun userId() = additionalInformation.userId
  fun userDisplayName() = additionalInformation.userDisplayName
}

enum class EventType(@JsonValue val eventType: String) {
  CIAG_INDUCTION_CREATED("ciag-induction.created"),
  CIAG_INDUCTION_UPDATED("ciag-induction.updated"),
}

data class AdditionalInformation(
  val reference: String,
  val prisonId: String,
  val occurredAt: Instant,
  val userId: String,
  val userDisplayName: String?,
)

data class PersonReference(val identifiers: List<Identifier>)

data class Identifier(val type: String, val value: String)
