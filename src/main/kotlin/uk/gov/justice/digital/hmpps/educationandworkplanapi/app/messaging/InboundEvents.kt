package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant

enum class InboundEventType(val eventType: String) {
  CIAG_INDUCTION_CREATED("ciag-induction.created") {
    override fun toInboundEvent(mapper: ObjectMapper, message: String) =
      mapper.readValue<CiagInductionCreatedEvent>(message)
  },
  CIAG_INDUCTION_UPDATED("ciag-induction.updated") {
    override fun toInboundEvent(mapper: ObjectMapper, message: String) =
      mapper.readValue<CiagInductionUpdatedEvent>(message)
  },
  ;

  abstract fun toInboundEvent(mapper: ObjectMapper, message: String): InboundEvent
}

interface InboundEvent {
  fun eventType(): String
  fun reference(): String
  fun prisonNumber(): String
  fun occurredAt(): Instant
}

// ------------ CIAG Induction events ------------------------------------------
abstract class CiagInductionEvent(
  private val personReference: PersonReference,
  private val additionalInformation: InductionInformation,
) : InboundEvent {
  override fun reference() = additionalInformation.reference
  override fun prisonNumber(): String = personReference.identifiers.first { it.type == "NOMS" }.value
  override fun occurredAt() = additionalInformation.occurredAt
  fun prisonId() = additionalInformation.prisonId
  fun userId() = additionalInformation.userId
  fun userDisplayName() = additionalInformation.userDisplayName
}

data class CiagInductionCreatedEvent(
  val personReference: PersonReference,
  val additionalInformation: InductionInformation,
) : CiagInductionEvent(personReference, additionalInformation) {
  @JsonGetter
  override fun eventType() = InboundEventType.CIAG_INDUCTION_CREATED.eventType
}

data class CiagInductionUpdatedEvent(
  val personReference: PersonReference,
  val additionalInformation: InductionInformation,
) : CiagInductionEvent(personReference, additionalInformation) {
  @JsonGetter
  override fun eventType() = InboundEventType.CIAG_INDUCTION_UPDATED.eventType
}

data class InductionInformation(
  val reference: String,
  val prisonId: String,
  val occurredAt: Instant,
  val userId: String,
  val userDisplayName: String?,
)

data class PersonReference(val identifiers: List<Identifier>)

data class Identifier(val type: String, val value: String)
