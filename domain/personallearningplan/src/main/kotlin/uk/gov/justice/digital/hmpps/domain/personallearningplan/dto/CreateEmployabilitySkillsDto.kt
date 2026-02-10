package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import java.time.LocalDate

data class CreateEmployabilitySkillsDto(
  val employabilitySkills: List<EmployabilitySkillDto>,
)

data class EmployabilitySkillDto(
  val prisonNumber: String,
  val prisonId: String,
  val employabilitySkillType: EmployabilitySkillType,
  val employabilitySkillRating: EmployabilitySkillRating,
  val activityName: String?,
  val evidence: String,
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
