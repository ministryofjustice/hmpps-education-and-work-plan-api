package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork
import java.util.UUID

fun aValidCreateWorkOnReleaseDto(
  hopingToWork: HopingToWork = HopingToWork.YES,
  affectAbilityToWork: List<AffectAbilityToWork> = emptyList(),
  affectAbilityToWorkOther: String? = null,
  prisonId: String = "BXI",
) = CreateWorkOnReleaseDto(
  hopingToWork = hopingToWork,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
  prisonId = prisonId,
)

fun aValidUpdateWorkOnReleaseDto(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.YES,
  affectAbilityToWork: List<AffectAbilityToWork> = emptyList(),
  affectAbilityToWorkOther: String? = null,
  prisonId: String = "BXI",
) = UpdateWorkOnReleaseDto(
  reference = reference,
  hopingToWork = hopingToWork,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
  prisonId = prisonId,
)
