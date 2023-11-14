package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

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
  val educationLevel: HighestEducationLevel?,
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

enum class HighestEducationLevel {
  PRIMARY_SCHOOL,
  SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  SECONDARY_SCHOOL_TOOK_EXAMS,
  FURTHER_EDUCATION_COLLEGE,
  UNDERGRADUATE_DEGREE_AT_UNIVERSITY,
  POSTGRADUATE_DEGREE_AT_UNIVERSITY,
  NOT_SURE,
}

data class Qualification(
  val subject: String,
  val level: QualificationLevel,
  val grade: String,
) : KeyAwareDomain {
  override fun key(): String = subject
}

enum class QualificationLevel {
  ENTRY_LEVEL,
  LEVEL_1,
  LEVEL_2,
  LEVEL_3,
  LEVEL_4,
  LEVEL_5,
  LEVEL_6,
  LEVEL_7,
  LEVEL_8,
}
