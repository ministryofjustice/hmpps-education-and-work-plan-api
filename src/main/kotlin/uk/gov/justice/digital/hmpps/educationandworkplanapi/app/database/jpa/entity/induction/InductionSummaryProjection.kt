package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

data class InductionSummaryProjection(
  val reference: UUID,
  val prisonNumber: String,
  val workOnRelease: WorkOnReleaseEntity,
  val createdAt: Instant,
  val createdBy: String,
  val updatedAt: Instant,
  val updatedBy: String,
)
