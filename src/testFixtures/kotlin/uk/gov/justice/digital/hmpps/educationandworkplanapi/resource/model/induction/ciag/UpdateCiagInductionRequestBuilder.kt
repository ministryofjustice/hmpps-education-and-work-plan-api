package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateEducationAndQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePrisonWorkAndEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateSkillsAndInterestsRequest
import java.util.UUID

fun aValidUpdateCiagInductionRequest(
  reference: UUID? = UUID.randomUUID(),
  hopingToGetWork: HopingToWork = HopingToWork.NOT_SURE,
  prisonId: String = "BXI",
  abilityToWork: Set<AbilityToWorkFactor>? = setOf(AbilityToWorkFactor.OTHER),
  abilityToWorkOther: String? = "Lack of interest",
  reasonToNotGetWork: Set<ReasonNotToWork>? = setOf(ReasonNotToWork.OTHER),
  reasonToNotGetWorkOther: String? = "Crime pays",
  workExperience: UpdatePreviousWorkRequest? = aValidUpdatePreviousWorkRequest(),
  skillsAndInterests: UpdateSkillsAndInterestsRequest? = aValidUpdateSkillsAndInterestsRequest(),
  qualificationsAndTraining: UpdateEducationAndQualificationsRequest? = aValidUpdateEducationAndQualificationsRequest(),
  inPrisonInterests: UpdatePrisonWorkAndEducationRequest? = aValidUpdatePrisonWorkAndEducationRequest(),
): UpdateCiagInductionRequest = UpdateCiagInductionRequest(
  reference = reference,
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
