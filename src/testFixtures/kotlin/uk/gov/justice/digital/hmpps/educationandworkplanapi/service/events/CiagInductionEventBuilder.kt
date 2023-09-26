package uk.gov.justice.digital.hmpps.educationandworkplanapi.service.events

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.CiagInductionCreatedEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.CiagInductionUpdatedEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.Identifier
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.InductionInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.PersonReference
import java.time.Instant
import java.util.UUID

fun aValidCiagInductionCreatedEvent(
  prisonNumber: String = aValidPrisonNumber(),
  reference: String = UUID.randomUUID().toString(),
  prisonId: String = "MDI",
  occurredAt: Instant = Instant.now(),
  userId: String = "rwest_gen",
  userDisplayName: String? = "Rose West",
): CiagInductionCreatedEvent =
  CiagInductionCreatedEvent(
    personReference = PersonReference(listOf(Identifier("NOMS", prisonNumber))),
    additionalInformation = InductionInformation(
      reference = reference,
      prisonId = prisonId,
      occurredAt = occurredAt,
      userId = userId,
      userDisplayName = userDisplayName,
    ),
  )

fun aValidCiagInductionUpdatedEvent(
  prisonNumber: String = aValidPrisonNumber(),
  reference: String = UUID.randomUUID().toString(),
  prisonId: String = "MDI",
  occurredAt: Instant = Instant.now(),
  userId: String = "rwest_gen",
  userDisplayName: String? = "Rose West",
): CiagInductionUpdatedEvent =
  CiagInductionUpdatedEvent(
    personReference = PersonReference(listOf(Identifier("NOMS", prisonNumber))),
    additionalInformation = InductionInformation(
      reference = reference,
      prisonId = prisonId,
      occurredAt = occurredAt,
      userId = userId,
      userDisplayName = userDisplayName,
    ),
  )
