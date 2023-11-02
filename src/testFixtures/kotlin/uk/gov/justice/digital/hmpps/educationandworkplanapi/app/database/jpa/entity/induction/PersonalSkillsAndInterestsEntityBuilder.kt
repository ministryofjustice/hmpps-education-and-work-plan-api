package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidPersonalSkillsAndInterestsEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  skills: MutableList<PersonalSkillEntity> = mutableListOf(aValidPersonalSkillEntity()),
  interests: MutableList<PersonalInterestEntity> = mutableListOf(aValidPersonalInterestEntity()),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  createdByDisplayName: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
  updatedByDisplayName: String? = null,
) =
  PersonalSkillsAndInterestsEntity(
    id = id,
    reference = reference,
    skills = skills,
    interests = interests,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )

fun aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  skills: MutableList<PersonalSkillEntity> = mutableListOf(aValidPersonalSkillEntity()),
  interests: MutableList<PersonalInterestEntity> = mutableListOf(aValidPersonalInterestEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  PersonalSkillsAndInterestsEntity(
    id = id,
    reference = reference,
    skills = skills,
    interests = interests,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
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
