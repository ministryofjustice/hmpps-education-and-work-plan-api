package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

/**
 * Holds details of any additional training that a Prisoner may have done.
 */
data class PreviousTraining(
  val trainingType: List<TrainingType>,
  val trainingTypeOther: String?,
)

enum class TrainingType {
  CSCS_CARD,
  FIRST_AID_CERTIFICATE,
  FOOD_HYGIENE_CERTIFICATE,
  FULL_UK_DRIVING_LICENCE,
  HEALTH_AND_SAFETY,
  HGV_LICENCE,
  MACHINERY_TICKETS,
  MANUAL_HANDLING,
  TRADE_COURSE,
  OTHER,
  NONE,
}
