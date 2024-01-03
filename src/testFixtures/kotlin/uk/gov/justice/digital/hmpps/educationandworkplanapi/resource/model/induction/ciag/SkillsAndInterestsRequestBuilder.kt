package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.ciag

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateSkillsAndInterestsRequest
import java.util.UUID

fun aValidCreateSkillsAndInterestsRequest(
  skills: Set<PersonalSkillType>? = setOf(PersonalSkillType.OTHER),
  skillsOther: String? = "Hidden skills",
  personalInterests: Set<PersonalInterestType>? = setOf(PersonalInterestType.OTHER),
  personalInterestsOther: String? = "Secret interests",
): CreateSkillsAndInterestsRequest =
  CreateSkillsAndInterestsRequest(
    skills = skills,
    skillsOther = skillsOther,
    personalInterests = personalInterests,
    personalInterestsOther = personalInterestsOther,
  )

fun aValidUpdateSkillsAndInterestsRequest(
  id: UUID? = UUID.randomUUID(),
  skills: Set<PersonalSkillType>? = setOf(PersonalSkillType.OTHER),
  skillsOther: String? = "Hidden skills",
  personalInterests: Set<PersonalInterestType>? = setOf(PersonalInterestType.OTHER),
  personalInterestsOther: String? = "Secret interests",
): UpdateSkillsAndInterestsRequest =
  UpdateSkillsAndInterestsRequest(
    id = id,
    skills = skills,
    skillsOther = skillsOther,
    personalInterests = personalInterests,
    personalInterestsOther = personalInterestsOther,
  )
