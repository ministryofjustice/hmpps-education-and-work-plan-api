package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork

fun aValidCreateCiagInductionRequest(
  hopingToGetWork: HopingToWork = HopingToWork.NOT_SURE,
  prisonId: String = "BXI",
  abilityToWork: Set<AbilityToWorkFactor>? = setOf(AbilityToWorkFactor.OTHER),
  abilityToWorkOther: String? = "Lack of interest",
  reasonToNotGetWork: Set<ReasonNotToWork>? = setOf(ReasonNotToWork.OTHER),
  reasonToNotGetWorkOther: String? = "Crime pays",
  workExperience: CreatePreviousWorkRequest? = aValidCreatePreviousWorkRequest(),
  skillsAndInterests: CreateSkillsAndInterestsRequest? = aValidCreateSkillsAndInterestsRequest(),
  qualificationsAndTraining: CreateEducationAndQualificationsRequest? = aValidCreateEducationAndQualificationsRequest(),
  inPrisonInterests: CreatePrisonWorkAndEducationRequest? = aValidCreatePrisonWorkAndEducationRequest(),
): CreateCiagInductionRequest = CreateCiagInductionRequest(
  hopingToGetWork = hopingToGetWork,
  prisonId = prisonId,
  reasonToNotGetWorkOther = reasonToNotGetWorkOther,
  abilityToWorkOther = abilityToWorkOther,
  abilityToWork = abilityToWork,
  reasonToNotGetWork = reasonToNotGetWork,
  workExperience = workExperience,
  skillsAndInterests = skillsAndInterests,
  qualificationsAndTraining = qualificationsAndTraining,
  inPrisonInterests = inPrisonInterests,
)
