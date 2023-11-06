package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SkillsAndInterestsResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidSkillsAndInterestsResponse(
  id: UUID? = UUID.randomUUID(),
  skills: Set<PersonalSkill>? = setOf(PersonalSkill.OTHER),
  skillsOther: String? = "Hidden skills",
  personalInterests: Set<PersonalInterest>? = setOf(PersonalInterest.OTHER),
  personalInterestsOther: String? = "Secret interests",
  modifiedBy: String = "auser_gen",
  modifiedDateTime: OffsetDateTime = OffsetDateTime.now(),
): SkillsAndInterestsResponse =
  SkillsAndInterestsResponse(
    id = id,
    skills = skills,
    skillsOther = skillsOther,
    personalInterests = personalInterests,
    personalInterestsOther = personalInterestsOther,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
