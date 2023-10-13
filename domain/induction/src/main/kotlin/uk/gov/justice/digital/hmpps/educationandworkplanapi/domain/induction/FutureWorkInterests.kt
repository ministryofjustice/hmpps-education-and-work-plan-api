package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant

/**
 * Represents a Prisoner's future work aspirations, including the type/sector of work and their desired role within it.
 */
data class FutureWorkInterests(
  val interests: List<WorkInterest>,
  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAt: Instant?,
  val lastUpdatedBy: String?,
  val lastUpdatedByDisplayName: String?,
  val lastUpdatedAt: Instant?,
)

data class WorkInterest(
  val workType: WorkInterestType,
  val workTypeOther: String?,
  val role: String,
)

enum class WorkInterestType {
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
