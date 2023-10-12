package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

fun aValidPersonalSkillsAndInterests(
  skills: List<PersonalSkill> = listOf(aValidPersonalSkill()),
  interests: List<PersonalInterest> = listOf(aValidPersonalInterest()),
) =
  PersonalSkillsAndInterests(
    skills = skills,
    interests = interests,
  )

fun aValidPersonalSkill(
  skillType: SkillType = SkillType.COMMUNICATION,
  skillTypeOther: String? = null,
) = PersonalSkill(
  skillType = skillType,
  skillTypeOther = skillTypeOther,
)

fun aValidPersonalInterest(
  interestType: InterestType = InterestType.CRAFTS,
  interestTypeOther: String? = null,
) = PersonalInterest(
  interestType = interestType,
  interestTypeOther = interestTypeOther,
)
