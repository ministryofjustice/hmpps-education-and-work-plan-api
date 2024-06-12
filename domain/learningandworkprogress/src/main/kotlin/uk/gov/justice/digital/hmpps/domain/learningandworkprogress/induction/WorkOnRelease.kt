package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

/**
 * Holds details of a Prisoner's work aspirations, including any barriers affecting their work.
 */
data class WorkOnRelease(
  val reference: UUID,
  val hopingToWork: HopingToWork,
  val affectAbilityToWork: List<AffectAbilityToWork>,
  val affectAbilityToWorkOther: String?,
  val createdBy: String?,
  val createdByDisplayName: String?,
  val createdAt: Instant?,
  val createdAtPrison: String,
  val lastUpdatedBy: String?,
  val lastUpdatedByDisplayName: String?,
  val lastUpdatedAt: Instant?,
  val lastUpdatedAtPrison: String,
)

enum class HopingToWork {
  YES,
  NO,
  NOT_SURE,
}

enum class AffectAbilityToWork {
  LIMITED_BY_OFFENCE,
  CARING_RESPONSIBILITIES,
  NEEDS_WORK_ADJUSTMENTS_DUE_TO_HEALTH,
  UNABLE_TO_WORK_DUE_TO_HEALTH,
  LACKS_CONFIDENCE_OR_MOTIVATION,
  REFUSED_SUPPORT_WITH_NO_REASON,
  RETIRED,
  NO_RIGHT_TO_WORK,
  NOT_SURE,
  OTHER,
  NONE,
}
