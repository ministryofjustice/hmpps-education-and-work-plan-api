package uk.gov.justice.digital.hmpps.educationandworkplanapi.service.events

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.Identifier
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.InboundEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.PersonReference
import java.time.Instant
import java.util.UUID

fun aValidCiagInductionCreatedEvent(
  prisonNumber: String = aValidPrisonNumber(),
  reference: String = UUID.randomUUID().toString(),
  prisonId: String = "MDI",
  userId: String = "rwest_gen",
  userDisplayName: String? = "Rose West",
  occurredAt: Instant = Instant.now(),
  version: Int = 1,
): InboundEvent =
  InboundEvent(
    eventType = EventType.CIAG_INDUCTION_CREATED,
    personReference = PersonReference(listOf(Identifier("NOMS", prisonNumber))),
    additionalInformation = AdditionalInformation(
      reference = reference,
      prisonId = prisonId,
      userId = userId,
      userDisplayName = userDisplayName,
    ),
    occurredAt = occurredAt,
    version = version,
  )

fun aValidCiagInductionUpdatedEvent(
  prisonNumber: String = aValidPrisonNumber(),
  reference: String = UUID.randomUUID().toString(),
  prisonId: String = "MDI",
  userId: String = "rwest_gen",
  userDisplayName: String? = "Rose West",
  occurredAt: Instant = Instant.now(),
  version: Int = 1,
): InboundEvent =
  InboundEvent(
    eventType = EventType.CIAG_INDUCTION_UPDATED,
    personReference = PersonReference(listOf(Identifier("NOMS", prisonNumber))),
    additionalInformation = AdditionalInformation(
      reference = reference,
      prisonId = prisonId,
      userId = userId,
      userDisplayName = userDisplayName,
    ),
    occurredAt = occurredAt,
    version = version,
  )
