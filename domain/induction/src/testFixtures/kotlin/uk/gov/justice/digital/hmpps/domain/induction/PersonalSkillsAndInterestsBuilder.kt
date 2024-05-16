package uk.gov.justice.digital.hmpps.domain.induction

import java.time.Instant
import java.util.UUID

fun aValidPersonalSkillsAndInterests(
  reference: UUID = UUID.randomUUID(),
  skills: List<PersonalSkill> = listOf(aValidPersonalSkill()),
  interests: List<PersonalInterest> = listOf(aValidPersonalInterest()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  PersonalSkillsAndInterests(
    reference = reference,
    skills = skills,
    interests = interests,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )

fun aValidPersonalSkill(
  skillType: SkillType = SkillType.OTHER,
  skillTypeOther: String? = "Hidden skills",
) = PersonalSkill(
  skillType = skillType,
  skillTypeOther = skillTypeOther,
)

fun aValidPersonalInterest(
  interestType: InterestType = InterestType.OTHER,
  interestTypeOther: String? = "Varied interests",
) = PersonalInterest(
  interestType = interestType,
  interestTypeOther = interestTypeOther,
)
