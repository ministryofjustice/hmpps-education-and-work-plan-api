package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.util.UUID

fun aValidPersonalSkillsAndInterestsEntity(
  reference: UUID = UUID.randomUUID(),
  skills: List<PersonalSkillEntity> = listOf(aValidPersonalSkillEntity()),
  interests: List<PersonalInterestEntity> = listOf(aValidPersonalInterestEntity()),
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
) =
  PersonalSkillsAndInterestsEntity(
    reference = reference,
    skills = skills,
    interests = interests,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  )

fun aValidPersonalSkillEntity(
  reference: UUID = UUID.randomUUID(),
  skillType: SkillType = SkillType.OTHER,
  skillTypeOther: String? = "None come to mind",
) =
  PersonalSkillEntity(
    reference = reference,
    skillType = skillType,
    skillTypeOther = skillTypeOther,
  )

fun aValidPersonalInterestEntity(
  reference: UUID = UUID.randomUUID(),
  interestType: InterestType = InterestType.OTHER,
  interestTypeOther: String? = "None come to mind",
) =
  PersonalInterestEntity(
    reference = reference,
    interestType = interestType,
    interestTypeOther = interestTypeOther,
  )
