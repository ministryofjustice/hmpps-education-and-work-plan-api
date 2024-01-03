package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AbilityToWorkFactor
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationAndQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HopingToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonWorkAndEducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonNotToWork
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SkillsAndInterestsResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidCiagInductionResponse(
  reference: UUID = UUID.randomUUID(),
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
  createdDateTime: OffsetDateTime = OffsetDateTime.now(),
  modifiedBy: String = "bjones_gen",
  modifiedDateTime: OffsetDateTime = OffsetDateTime.now(),
): CiagInductionResponse =
  CiagInductionResponse(
    reference = reference,
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
