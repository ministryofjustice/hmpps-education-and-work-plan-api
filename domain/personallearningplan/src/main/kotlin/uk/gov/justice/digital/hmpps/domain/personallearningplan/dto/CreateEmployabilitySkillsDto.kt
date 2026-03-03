package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

data class CreateEmployabilitySkillsDto(
  val employabilitySkills: List<EmployabilitySkillDto>,
)

data class EmployabilitySkillDto(
  val prisonNumber: String,
  val prisonId: String,
  val employabilitySkillType: EmployabilitySkillType,
  val employabilitySkillRating: EmployabilitySkillRating,
  val evidence: String,
  val sessionType: EmployabilitySkillSessionType? = null,
  val sessionTypeDescription: String? = null,
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

enum class EmployabilitySkillSessionType {
  CIAG_INDUCTION,
  CIAG_REVIEW,
  EDUCATION_REVIEW,
  INDUSTRIES_REVIEW,
}

enum class EmployabilitySkillRating {
  NOT_CONFIDENT,
  LITTLE_CONFIDENCE,
  QUITE_CONFIDENT,
  VERY_CONFIDENT,
}
