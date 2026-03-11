package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Holds details of an education assessment event received from an external source (e.g. Curious).
 *
 * These records are immutable once created. Multiple records can exist per prisoner, one per event received.
 */
data class EducationAssessmentEvent(
  val reference: UUID,
  val prisonNumber: String,
  val statusChangeDate: LocalDate,
  val status: EducationAssessmentStatus,
  val source: String,
  val detailUrl: String?,
  val createdBy: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val lastUpdatedBy: String,
  val lastUpdatedAt: Instant,
  val lastUpdatedAtPrison: String,
)

enum class EducationAssessmentStatus {
  ALL_RELEVANT_ASSESSMENTS_COMPLETE,
}
