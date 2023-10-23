package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import aValidPrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequestData
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SkillsAndInterestsRequest

fun aValidCreateCiagInductionRequestData(
  hopingToGetWork: HopingToWork = HopingToWork.NOT_SURE,
  prisonId: String? = "BXI",
  prisonName: String? = "HMP Brixton",
  reasonToNotGetWorkOther: String? = "Crime pays",
  abilityToWorkOther: String? = "Lack of interest",
  abilityToWork: Set<AbilityToWorkFactor>? = setOf(AbilityToWorkFactor.OTHER),
  reasonToNotGetWork: Set<ReasonNotToWork>? = setOf(ReasonNotToWork.OTHER),
  workExperience: PreviousWorkRequest? = aValidPreviousWorkRequest(),
  skillsAndInterests: SkillsAndInterestsRequest? = aValidSkillsAndInterestsRequest(),
  qualificationsAndTraining: EducationAndQualificationsRequest? = aValidEducationAndQualificationsRequest(),
  inPrisonInterests: PrisonWorkAndEducationRequest? = aValidPrisonWorkAndEducationRequest(),
): CreateCiagInductionRequestData = CreateCiagInductionRequestData(
  hopingToGetWork = hopingToGetWork,
  prisonId = prisonId,
  prisonName = prisonName,
  reasonToNotGetWorkOther = reasonToNotGetWorkOther,
  abilityToWorkOther = abilityToWorkOther,
  abilityToWork = abilityToWork,
  reasonToNotGetWork = reasonToNotGetWork,
  workExperience = workExperience,
  skillsAndInterests = skillsAndInterests,
  qualificationsAndTraining = qualificationsAndTraining,
  inPrisonInterests = inPrisonInterests,
)
