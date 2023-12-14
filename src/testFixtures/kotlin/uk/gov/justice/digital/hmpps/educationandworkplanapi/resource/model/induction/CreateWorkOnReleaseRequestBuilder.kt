package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NotHopingToWorkReason

fun aValidCreateWorkOnReleaseRequest(
  hopingToWork: HopingToWork = HopingToWork.NO,
  notHopingToWorkReasons: List<NotHopingToWorkReason>? = listOf(NotHopingToWorkReason.OTHER),
  notHopingToWorkOtherReason: String? = "Long term prison sentence",
  affectAbilityToWork: List<AffectAbilityToWork>? = listOf(AffectAbilityToWork.OTHER),
  affectAbilityToWorkOther: String? = "Employers aren't interested",
): CreateWorkOnReleaseRequest =
  CreateWorkOnReleaseRequest(
    hopingToWork = hopingToWork,
    notHopingToWorkReasons = notHopingToWorkReasons,
    notHopingToWorkOtherReason = notHopingToWorkOtherReason,
    affectAbilityToWork = affectAbilityToWork,
    affectAbilityToWorkOther = affectAbilityToWorkOther,
  )
