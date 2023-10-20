package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant
import java.util.UUID

/**
 * Holds details of any additional training that a Prisoner may have done.
 *
 * Note that the list of training cannot be empty, since NONE is an option.
 */
data class PreviousTraining(
  val reference: UUID?,
  val trainingTypes: List<TrainingType>,
  val trainingTypeOther: String?,
  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAt: Instant?,
  val createdAtPrison: String,
  val lastUpdatedBy: String?,
  val lastUpdatedByDisplayName: String?,
  val lastUpdatedAt: Instant?,
  val lastUpdatedAtPrison: String,
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
