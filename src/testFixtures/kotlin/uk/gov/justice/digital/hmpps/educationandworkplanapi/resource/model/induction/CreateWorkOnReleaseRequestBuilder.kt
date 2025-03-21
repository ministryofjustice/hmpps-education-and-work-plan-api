package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AffectAbilityToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkOnReleaseRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork

fun aValidCreateWorkOnReleaseRequest(): CreateWorkOnReleaseRequest = aValidCreateWorkOnReleaseRequestForPrisonerNotLookingToWork()

fun aValidCreateWorkOnReleaseRequestForPrisonerNotLookingToWork(
  hopingToWork: HopingToWork = HopingToWork.NO,
  affectAbilityToWork: List<AffectAbilityToWork>? = listOf(AffectAbilityToWork.OTHER),
  affectAbilityToWorkOther: String? = "Employers aren't interested",
): CreateWorkOnReleaseRequest = CreateWorkOnReleaseRequest(
  hopingToWork = hopingToWork,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
)

fun aValidCreateWorkOnReleaseRequestForPrisonerLookingToWork(
  hopingToWork: HopingToWork = HopingToWork.YES,
  affectAbilityToWork: List<AffectAbilityToWork>? = listOf(AffectAbilityToWork.NONE),
  affectAbilityToWorkOther: String? = null,
): CreateWorkOnReleaseRequest = CreateWorkOnReleaseRequest(
  hopingToWork = hopingToWork,
  affectAbilityToWork = affectAbilityToWork,
  affectAbilityToWorkOther = affectAbilityToWorkOther,
)
