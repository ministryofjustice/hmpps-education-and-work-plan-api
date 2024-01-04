package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePersonalSkillsAndInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterestType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalSkillType

fun aValidCreatePersonalSkillsAndInterestsRequest(
  skills: List<PersonalSkill> = listOf(aValidPersonalSkill()),
  interests: List<PersonalInterest> = listOf(aValidPersonalInterest()),
): CreatePersonalSkillsAndInterestsRequest =
  CreatePersonalSkillsAndInterestsRequest(
    skills = skills,
    interests = interests,
  )

fun aValidPersonalSkill(
  skillType: PersonalSkillType = PersonalSkillType.OTHER,
  skillTypeOther: String? = "Hidden skills",
): PersonalSkill = PersonalSkill(
  skillType = skillType,
  skillTypeOther = skillTypeOther,
)

fun aValidPersonalInterest(
  interestType: PersonalInterestType = PersonalInterestType.OTHER,
  interestTypeOther: String? = "Varied interests",
): PersonalInterest = PersonalInterest(
  interestType = interestType,
  interestTypeOther = interestTypeOther,
)
