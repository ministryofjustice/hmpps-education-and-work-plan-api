package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidInductionSummaryProjection(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  updatedBy: String = "bjones_gen",
  updatedAt: Instant = Instant.now(),
): InductionSummaryProjection = InductionSummaryProjection(
  reference = reference,
  prisonNumber = prisonNumber,
  createdBy = createdBy,
  createdAt = createdAt,
  updatedBy = updatedBy,
  updatedAt = updatedAt,
)
