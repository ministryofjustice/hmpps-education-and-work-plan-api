package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

/**
 * Holds details of a Prisoner's work aspirations, including any barriers affecting their work.
 */
data class WorkOnRelease(
  val hopingToWork: HopingToWork,
  val notHopingToWorkReasons: List<NotHopingToWorkReason>,
  val notHopingToWorkOtherReason: String?,
  val affectAbilityToWork: List<AffectAbilityToWork>,
  val affectAbilityToWorkOther: String?,
)

enum class HopingToWork {
  YES,
  NO,
  NOT_SURE,
}

enum class NotHopingToWorkReason {
  LIMIT_THEIR_ABILITY,
  FULL_TIME_CARER,
  LACKS_CONFIDENCE_OR_MOTIVATION,
  HEALTH,
  RETIRED,
  NO_RIGHT_TO_WORK,
  NOT_SURE,
  OTHER,
  NO_REASON,
}

enum class AffectAbilityToWork {
  CARING_RESPONSIBILITIES,
  LIMITED_BY_OFFENSE,
  HEALTH_ISSUES,
  NO_RIGHT_TO_WORK,
  OTHER,
  NONE,
}
