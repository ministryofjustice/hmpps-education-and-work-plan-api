package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.LocalDateTime

fun aValidCiagInductionResponse(
  offenderId: String = aValidPrisonNumber(),
  prisonId: String? = "BXI",
  hopingToGetWork: HopingToWork = HopingToWork.NOT_SURE,
  desireToWork: Boolean? = false,
  abilityToWork: Set<AbilityToWorkFactor>? = setOf(AbilityToWorkFactor.OTHER),
  abilityToWorkOther: String? = "Mental health issues",
  reasonToNotGetWork: Set<ReasonNotToWork>? = setOf(ReasonNotToWork.OTHER),
  reasonToNotGetWorkOther: String? = "Crime pays",
  workExperience: PreviousWorkResponse? = aValidPreviousWorkResponse(),
  skillsAndInterests: SkillsAndInterestsResponse? = aValidSkillsAndInterestsResponse(),
  qualificationsAndTraining: EducationAndQualificationResponse? = aValidEducationAndQualificationsResponse(),
  inPrisonInterests: PrisonWorkAndEducationResponse? = aValidPrisonWorkAndEducationResponse(),
  createdBy: String = "asmith_gen",
  createdDateTime: LocalDateTime = LocalDateTime.now(),
  modifiedBy: String = "bjones_gen",
  modifiedDateTime: LocalDateTime = LocalDateTime.now(),
): CiagInductionResponse =
  CiagInductionResponse(
    offenderId = offenderId,
    prisonId = prisonId,
    hopingToGetWork = hopingToGetWork,
    desireToWork = desireToWork,
    reasonToNotGetWorkOther = reasonToNotGetWorkOther,
    abilityToWork = abilityToWork,
    abilityToWorkOther = abilityToWorkOther,
    reasonToNotGetWork = reasonToNotGetWork,
    workExperience = workExperience,
    skillsAndInterests = skillsAndInterests,
    qualificationsAndTraining = qualificationsAndTraining,
    inPrisonInterests = inPrisonInterests,
    createdBy = createdBy,
    createdDateTime = createdDateTime,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
