package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

/**
 * Contains details of a Prisoner's work experience, if applicable.
 */
data class PreviousWorkExperiences(
  val hasWorkBefore: Boolean,
  val experiences: List<WorkExperience>,
)

data class WorkExperience(
  val experienceType: WorkExperienceType,
  val experienceTypeOther: String?,
  val role: String?,
  val details: String?,
)

enum class WorkExperienceType {
  OUTDOOR,
  CONSTRUCTION,
  DRIVING,
  BEAUTY,
  HOSPITALITY,
  TECHNICAL,
  MANUFACTURING,
  OFFICE,
  RETAIL,
  SPORTS,
  WAREHOUSING,
  WASTE_MANAGEMENT,
  EDUCATION_TRAINING,
  CLEANING_AND_MAINTENANCE,
  OTHER,
}
