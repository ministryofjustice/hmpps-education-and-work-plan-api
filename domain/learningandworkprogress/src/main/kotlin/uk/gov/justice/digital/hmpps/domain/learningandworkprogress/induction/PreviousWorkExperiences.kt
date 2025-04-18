package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

/**
 * Contains details of a Prisoner's work experience, if applicable.
 *
 * Note that if the list of `experiences` is empty, then the Prisoner has been asked if they have any work history,
 * but either they do not, or they do not wish to provide details.
 */
data class PreviousWorkExperiences(
  val reference: UUID,
  val hasWorkedBefore: HasWorkedBefore,
  val hasWorkedBeforeNotRelevantReason: String?,
  val experiences: List<WorkExperience>,
  val createdBy: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val lastUpdatedBy: String,
  val lastUpdatedAt: Instant,
  val lastUpdatedAtPrison: String,
)

data class WorkExperience(
  val experienceType: WorkExperienceType,
  val experienceTypeOther: String?,
  val role: String?,
  val details: String?,
) : KeyAwareDomain {
  override fun key(): String = experienceType.name
}

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

enum class HasWorkedBefore {
  YES,
  NO,
  NOT_RELEVANT,
}
