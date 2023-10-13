package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.time.Instant

fun aValidPersonalSkillsAndInterests(
  skills: List<PersonalSkill> = listOf(aValidPersonalSkill()),
  interests: List<PersonalInterest> = listOf(aValidPersonalInterest()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
) =
  PersonalSkillsAndInterests(
    skills = skills,
    interests = interests,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
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
