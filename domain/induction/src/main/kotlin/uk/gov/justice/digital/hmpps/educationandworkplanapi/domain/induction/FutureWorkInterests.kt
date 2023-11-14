package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant
import java.util.UUID

/**
 * Represents a Prisoner's future work aspirations, including the type/sector of work and their desired role within it.
 *
 * Note that this domain model allows for the list of interests to be empty, even if at least one currently has to be
 * provided on screen. This allows us to cater for the scenario where the Prisoner has been asked if they have
 * interests, but either they do not, or they do not wish to provide details. In other words, the domain is not
 * modelled on the current screen behaviour.
 */
data class FutureWorkInterests(
  val reference: UUID,
  val interests: List<WorkInterest>,
  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAt: Instant?,
  val createdAtPrison: String,
  val lastUpdatedBy: String?,
  val lastUpdatedByDisplayName: String?,
  val lastUpdatedAt: Instant?,
  val lastUpdatedAtPrison: String,
)

data class WorkInterest(
  val workType: WorkInterestType,
  val workTypeOther: String?,
  val role: String?,
) : KeyAwareDomain {
  override fun key(): String = workType.name
}

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
