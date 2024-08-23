package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education

import java.time.Instant
import java.util.UUID

/**
 * Holds details about a Prisoner's educational qualifications, including where relevant, the grades achieved in each
 * subject.
 *
 * Note that the list of `qualifications` can be empty, but `educationLevel` is mandatory (but only if the Prisoner has
 * been asked about their education).
 */
data class PreviousQualifications(
  val reference: UUID,
  val prisonNumber: String,
  val educationLevel: EducationLevel?,
  val qualifications: List<Qualification>,
  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAt: Instant?,
  val createdAtPrison: String,
  val lastUpdatedBy: String?,
  val lastUpdatedByDisplayName: String?,
  val lastUpdatedAt: Instant?,
  val lastUpdatedAtPrison: String,
)

data class Qualification(
  val reference: UUID,
  val subject: String,
  val level: QualificationLevel,
  val grade: String,
  val createdBy: String,
  val createdAt: Instant,
  val lastUpdatedBy: String,
  val lastUpdatedAt: Instant,
)
