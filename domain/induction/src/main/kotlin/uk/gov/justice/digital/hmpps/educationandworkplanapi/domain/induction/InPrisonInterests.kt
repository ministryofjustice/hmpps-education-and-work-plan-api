package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant
import java.util.UUID

/**
 * Represents any in-prison work or training interests a Prisoner might have during their time in prison.
 *
 * Note that this domain model allows for the lists of interests to be empty, even if at least one of each currently
 * has to be provided on screen. This allows us to cater for the scenario where the Prisoner has been asked if they
 * have any, but either they do not, or they do not wish to provide details. In other words, the domain is not
 * modelled on the current screen behaviour.
 */
data class InPrisonInterests(
  val reference: UUID,
  val inPrisonWorkInterests: List<InPrisonWorkInterest>,
  val inPrisonTrainingInterests: List<InPrisonTrainingInterest>,
  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAt: Instant?,
  val createdAtPrison: String,
  val lastUpdatedBy: String?,
  val lastUpdatedByDisplayName: String?,
  val lastUpdatedAt: Instant?,
  val lastUpdatedAtPrison: String,
)

data class InPrisonWorkInterest(
  val workType: InPrisonWorkType,
  val workTypeOther: String?,
) : DomainKeyAware {
  override fun key(): String = workType.name
}

data class InPrisonTrainingInterest(
  val trainingType: InPrisonTrainingType,
  val trainingTypeOther: String?,
) : DomainKeyAware {
  override fun key(): String = trainingType.name
}

enum class InPrisonWorkType {
  CLEANING_AND_HYGIENE,
  COMPUTERS_OR_DESK_BASED,
  GARDENING_AND_OUTDOORS,
  KITCHENS_AND_COOKING,
  MAINTENANCE,
  PRISON_LAUNDRY,
  PRISON_LIBRARY,
  TEXTILES_AND_SEWING,
  WELDING_AND_METALWORK,
  WOODWORK_AND_JOINERY,
  OTHER,
}

enum class InPrisonTrainingType {
  BARBERING_AND_HAIRDRESSING,
  CATERING,
  COMMUNICATION_SKILLS,
  ENGLISH_LANGUAGE_SKILLS,
  FORKLIFT_DRIVING,
  INTERVIEW_SKILLS,
  MACHINERY_TICKETS,
  NUMERACY_SKILLS,
  RUNNING_A_BUSINESS,
  SOCIAL_AND_LIFE_SKILLS,
  WELDING_AND_METALWORK,
  WOODWORK_AND_JOINERY,
  OTHER,
}
