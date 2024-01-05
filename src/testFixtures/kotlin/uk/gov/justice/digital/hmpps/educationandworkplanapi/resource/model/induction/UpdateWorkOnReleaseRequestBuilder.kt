package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NotHopingToWorkReason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateWorkOnReleaseRequest
import java.util.UUID

fun aValidUpdateWorkOnReleaseRequest(): UpdateWorkOnReleaseRequest =
  aValidUpdateWorkOnReleaseRequestForPrisonerNotLookingToWork()

fun aValidUpdateWorkOnReleaseRequestForPrisonerNotLookingToWork(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.NO,
  notHopingToWorkReasons: List<NotHopingToWorkReason>? = listOf(NotHopingToWorkReason.OTHER),
  notHopingToWorkOtherReason: String? = "Long term prison sentence",
  affectAbilityToWork: List<AffectAbilityToWork>? = listOf(AffectAbilityToWork.OTHER),
  affectAbilityToWorkOther: String? = "Employers aren't interested",
): UpdateWorkOnReleaseRequest =
  UpdateWorkOnReleaseRequest(
    reference = reference,
    hopingToWork = hopingToWork,
    notHopingToWorkReasons = notHopingToWorkReasons,
    notHopingToWorkOtherReason = notHopingToWorkOtherReason,
    affectAbilityToWork = affectAbilityToWork,
    affectAbilityToWorkOther = affectAbilityToWorkOther,
  )

fun aValidUpdateWorkOnReleaseRequestForPrisonerLookingToWork(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.YES,
  notHopingToWorkReasons: List<NotHopingToWorkReason>? = null,
  notHopingToWorkOtherReason: String? = null,
  affectAbilityToWork: List<AffectAbilityToWork>? = listOf(AffectAbilityToWork.NONE),
  affectAbilityToWorkOther: String? = null,
): UpdateWorkOnReleaseRequest =
  UpdateWorkOnReleaseRequest(
    reference = reference,
    hopingToWork = hopingToWork,
    notHopingToWorkReasons = notHopingToWorkReasons,
    notHopingToWorkOtherReason = notHopingToWorkOtherReason,
    affectAbilityToWork = affectAbilityToWork,
    affectAbilityToWorkOther = affectAbilityToWorkOther,
  )
