package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import java.time.LocalDateTime

fun aValidSkillsAndInterestsResponse(
  skills: Set<PersonalSkill>? = setOf(PersonalSkill.OTHER),
  skillsOther: String? = "Hidden skills",
  personalInterests: Set<PersonalInterest>? = setOf(PersonalInterest.OTHER),
  personalInterestsOther: String? = "Secret interests",
  modifiedBy: String = "bjones_gen",
  modifiedDateTime: LocalDateTime = LocalDateTime.now(),
): SkillsAndInterestsResponse =
  SkillsAndInterestsResponse(
    skills = skills,
    skillsOther = skillsOther,
    personalInterests = personalInterests,
    personalInterestsOther = personalInterestsOther,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
