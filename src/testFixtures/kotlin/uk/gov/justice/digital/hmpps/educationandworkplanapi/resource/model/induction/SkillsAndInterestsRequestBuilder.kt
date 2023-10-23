package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SkillsAndInterestsRequest

fun aValidSkillsAndInterestsRequest(
  skills: Set<PersonalSkill>? = setOf(PersonalSkill.OTHER),
  skillsOther: String? = "Hidden skills",
  personalInterests: Set<PersonalInterest>? = setOf(PersonalInterest.OTHER),
  personalInterestsOther: String? = "Secret interests",
): SkillsAndInterestsRequest =
  SkillsAndInterestsRequest(
    skills = skills,
    skillsOther = skillsOther,
    personalInterests = personalInterests,
    personalInterestsOther = personalInterestsOther,
  )
