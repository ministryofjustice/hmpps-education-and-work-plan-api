package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

fun aValidInductionSummary(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
): InductionSummary = InductionSummary(
  reference = reference,
  prisonNumber = prisonNumber,
  createdBy = createdBy,
  createdAt = createdAt,
  lastUpdatedBy = lastUpdatedBy,
  lastUpdatedAt = lastUpdatedAt,
)
