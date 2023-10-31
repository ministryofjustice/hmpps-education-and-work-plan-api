package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateSkillsAndInterestsRequest
import java.util.UUID

fun aValidCreateSkillsAndInterestsRequest(
  skills: Set<PersonalSkill>? = setOf(PersonalSkill.OTHER),
  skillsOther: String? = "Hidden skills",
  personalInterests: Set<PersonalInterest>? = setOf(PersonalInterest.OTHER),
  personalInterestsOther: String? = "Secret interests",
): CreateSkillsAndInterestsRequest =
  CreateSkillsAndInterestsRequest(
    skills = skills,
    skillsOther = skillsOther,
    personalInterests = personalInterests,
    personalInterestsOther = personalInterestsOther,
  )

fun aValidUpdateSkillsAndInterestsRequest(
  id: UUID = UUID.randomUUID(),
  skills: Set<PersonalSkill>? = setOf(PersonalSkill.OTHER),
  skillsOther: String? = "Hidden skills",
  personalInterests: Set<PersonalInterest>? = setOf(PersonalInterest.OTHER),
  personalInterestsOther: String? = "Secret interests",
): UpdateSkillsAndInterestsRequest =
  UpdateSkillsAndInterestsRequest(
    id = id,
    skills = skills,
    skillsOther = skillsOther,
    personalInterests = personalInterests,
    personalInterestsOther = personalInterestsOther,
  )
