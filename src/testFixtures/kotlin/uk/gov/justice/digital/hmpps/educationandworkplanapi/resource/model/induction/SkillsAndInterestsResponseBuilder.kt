package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SkillsAndInterestsResponse
import java.time.OffsetDateTime
import java.util.UUID

fun aValidSkillsAndInterestsResponse(
  id: UUID? = UUID.randomUUID(),
  skills: Set<PersonalSkillType>? = setOf(PersonalSkillType.OTHER),
  skillsOther: String? = "Hidden skills",
  personalInterests: Set<PersonalInterestType>? = setOf(PersonalInterestType.OTHER),
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
