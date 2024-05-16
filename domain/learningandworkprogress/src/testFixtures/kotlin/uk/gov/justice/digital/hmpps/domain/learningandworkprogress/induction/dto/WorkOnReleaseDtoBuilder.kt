package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.NotHopingToWorkReason
import java.util.UUID

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

fun aValidUpdateWorkOnReleaseDto(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.YES,
  notHopingToWorkReasons: List<NotHopingToWorkReason> = emptyList(),
  notHopingToWorkOtherReason: String? = null,
  affectAbilityToWork: List<AffectAbilityToWork> = emptyList(),
  affectAbilityToWorkOther: String? = null,
  prisonId: String = "BXI",
) = UpdateWorkOnReleaseDto(
  reference = reference,
  hopingToWork = hopingToWork,
  notHopingToWorkReasons = notHopingToWorkReasons,
  notHopingToWorkOtherReason = notHopingToWorkOtherReason,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
  prisonId = prisonId,
)
