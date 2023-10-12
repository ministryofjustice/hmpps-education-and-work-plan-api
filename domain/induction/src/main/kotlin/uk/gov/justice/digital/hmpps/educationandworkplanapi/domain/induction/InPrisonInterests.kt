package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

/**
 * Represents any in-prison work or training interests a Prisoner might have during their time in prison.
 */
data class InPrisonInterests(
  val inPrisonWorkInterests: List<InPrisonWorkInterest>,
  val inPrisonTrainingInterests: List<InPrisonTrainingInterest>,
)

data class InPrisonWorkInterest(
  val workType: InPrisonWorkType,
  val workTypeOther: String?,
)

data class InPrisonTrainingInterest(
  val trainingType: InPrisonTrainingType,
  val trainingTypeOther: String?,
)

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
