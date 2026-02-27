package uk.gov.justice.digital.hmpps.domain.personallearningplan

import java.time.Instant
import java.util.UUID

/**
 * Represents a Prisoner's 'Employability skill'.
 */
class EmployabilitySkill(
  val reference: UUID,
  val prisonNumber: String,
  val employabilitySkillType: EmployabilitySkillType,
  val sessionType: String?,
  val sessionTypeDescription: String?,
  val ratingCode: String,
  val evidence: String,
  val createdAtPrison: String,
  val updatedAtPrison: String,
  val createdBy: String,
  val createdAt: Instant,
  val updatedBy: String,
  val updatedAt: Instant,

)

enum class EmployabilitySkillType {
  TEAMWORK,
  TIMEKEEPING,
  COMMUNICATION,
  PLANNING,
  ORGANISATION,
  PROBLEM_SOLVING,
  INITIATIVE,
  ADAPTABILITY,
  RELIABILITY,
  CREATIVITY,
}

enum class EmployabilitySkillRating {
  NOT_CONFIDENT,
  LITTLE_CONFIDENCE,
  QUITE_CONFIDENT,
  VERY_CONFIDENT,
}
