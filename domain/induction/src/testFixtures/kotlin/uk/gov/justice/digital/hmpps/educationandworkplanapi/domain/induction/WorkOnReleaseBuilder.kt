package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

fun aValidWorkOnRelease(
  hopingToWork: HopingToWork = HopingToWork.YES,
  notHopingToWorkReasons: List<NotHopingToWorkReason> = emptyList(),
  notHopingToWorkOtherReason: String? = null,
  affectAbilityToWork: List<AffectAbilityToWork> = emptyList(),
  affectAbilityToWorkOther: String? = null,
) = WorkOnRelease(
  hopingToWork = hopingToWork,
  notHopingToWorkReasons = notHopingToWorkReasons,
  notHopingToWorkOtherReason = notHopingToWorkOtherReason,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
)
