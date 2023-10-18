package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.NotHopingToWorkReason

fun aValidCreateWorkOnReleaseDto(
  hopingToWork: HopingToWork = HopingToWork.YES,
  notHopingToWorkReasons: List<NotHopingToWorkReason> = emptyList(),
  notHopingToWorkOtherReason: String? = null,
  affectAbilityToWork: List<AffectAbilityToWork> = emptyList(),
  affectAbilityToWorkOther: String? = null,
  prisonId: String = "BXI",
) = CreateWorkOnReleaseDto(
  hopingToWork = hopingToWork,
  notHopingToWorkReasons = notHopingToWorkReasons,
  notHopingToWorkOtherReason = notHopingToWorkOtherReason,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
  prisonId = prisonId,
)
