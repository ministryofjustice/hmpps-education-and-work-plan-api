package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateWorkOnReleaseRequest
import java.util.UUID

fun aValidUpdateWorkOnReleaseRequest(): UpdateWorkOnReleaseRequest =
  aValidUpdateWorkOnReleaseRequestForPrisonerNotLookingToWork()

fun aValidUpdateWorkOnReleaseRequestForPrisonerNotLookingToWork(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.NO,
  affectAbilityToWork: List<AffectAbilityToWork>? = listOf(AffectAbilityToWork.OTHER),
  affectAbilityToWorkOther: String? = "Employers aren't interested",
): UpdateWorkOnReleaseRequest =
  UpdateWorkOnReleaseRequest(
    reference = reference,
    hopingToWork = hopingToWork,
    affectAbilityToWork = affectAbilityToWork,
    affectAbilityToWorkOther = affectAbilityToWorkOther,
  )

fun aValidUpdateWorkOnReleaseRequestForPrisonerLookingToWork(
  reference: UUID = UUID.randomUUID(),
  hopingToWork: HopingToWork = HopingToWork.YES,
  affectAbilityToWork: List<AffectAbilityToWork>? = listOf(AffectAbilityToWork.NONE),
  affectAbilityToWorkOther: String? = null,
): UpdateWorkOnReleaseRequest =
  UpdateWorkOnReleaseRequest(
    reference = reference,
    hopingToWork = hopingToWork,
    affectAbilityToWork = affectAbilityToWork,
    affectAbilityToWorkOther = affectAbilityToWorkOther,
  )
