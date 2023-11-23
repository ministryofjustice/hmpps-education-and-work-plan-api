package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant
import java.util.UUID

/**
 * A subset of a Prisoner's Induction (mainly for performance reasons so that a large number of these can be
 * provided - e.g. in an HTTP response).
 */
data class InductionSummary(
  val reference: UUID,
  val prisonNumber: String,
  val workOnRelease: WorkOnRelease,
  val createdBy: String,
  val createdAt: Instant,
  val lastUpdatedBy: String,
  val lastUpdatedAt: Instant,
)
