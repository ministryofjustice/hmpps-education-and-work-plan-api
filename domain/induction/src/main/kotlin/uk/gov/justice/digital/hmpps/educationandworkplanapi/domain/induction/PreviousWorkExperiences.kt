package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant

/**
 * Contains details of a Prisoner's work experience, if applicable.
 *
 * Note that if the list of `experiences` is empty, then the Prisoner has been asked if they have any work history,
 * but either they do not, or they do not wish to provide details.
 */
data class PreviousWorkExperiences(
  val experiences: List<WorkExperience>,
  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAt: Instant?,
  val lastUpdatedBy: String?,
  val lastUpdatedByDisplayName: String?,
  val lastUpdatedAt: Instant?,
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
