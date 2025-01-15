package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import java.time.Instant
import java.util.UUID

fun aValidPersonalSkillsAndInterests(
  reference: UUID = UUID.randomUUID(),
  skills: List<PersonalSkill> = listOf(aValidPersonalSkill()),
  interests: List<PersonalInterest> = listOf(aValidPersonalInterest()),
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  PersonalSkillsAndInterests(
    reference = reference,
    skills = skills,
    interests = interests,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
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
