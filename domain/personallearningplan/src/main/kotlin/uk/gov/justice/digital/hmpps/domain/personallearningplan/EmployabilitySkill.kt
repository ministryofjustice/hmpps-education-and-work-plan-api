package uk.gov.justice.digital.hmpps.domain.personallearningplan

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Represents a Prisoner's 'Employability skill'.
 */
class EmployabilitySkill(
  val reference: UUID,
  val prisonNumber: String,
  val employabilitySkillType: EmployabilitySkillType,
  val ratingCode: String,
  val activityName: String,
  val evidence: String,
  val createdAtPrison: String,
  val updatedAtPrison: String,
  val createdBy: String,
  val createdAt: Instant,
  val updatedBy: String,
  val updatedAt: Instant,
  val conversationDate: LocalDate? = null,

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
