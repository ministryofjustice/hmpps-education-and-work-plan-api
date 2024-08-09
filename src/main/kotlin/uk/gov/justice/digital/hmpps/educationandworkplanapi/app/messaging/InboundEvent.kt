package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.RawJsonDeserializer
import java.time.Instant

data class InboundEvent(
  val eventType: EventType,
  val personReference: PersonReference,
  @JsonDeserialize(using = RawJsonDeserializer::class) val additionalInformation: String,
  val occurredAt: Instant,
  val publishedAt: Instant,
  val description: String,
  val version: String,
) {
  fun prisonNumber(): String = personReference.identifiers.first { it.type == "NOMS" }.value
}

data class PersonReference(val identifiers: List<Identifier>)

data class Identifier(val type: String, val value: String)
