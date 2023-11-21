package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

import java.time.Instant
import java.util.UUID

fun aValidPersonalSkillsAndInterestsMigrationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  skills: MutableList<PersonalSkillMigrationEntity> = mutableListOf(aValidPersonalSkillMigrationEntity()),
  interests: MutableList<PersonalInterestMigrationEntity> = mutableListOf(aValidPersonalInterestMigrationEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  PersonalSkillsAndInterestsMigrationEntity(
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

fun aValidPersonalSkillMigrationEntity(
  reference: UUID = UUID.randomUUID(),
  skillType: SkillType = SkillType.OTHER,
  skillTypeOther: String? = "None come to mind",
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
) =
  PersonalSkillMigrationEntity(
    reference = reference,
    skillType = skillType,
    skillTypeOther = skillTypeOther,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )

fun aValidPersonalInterestMigrationEntity(
  reference: UUID = UUID.randomUUID(),
  interestType: InterestType = InterestType.OTHER,
  interestTypeOther: String? = "None come to mind",
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
) =
  PersonalInterestMigrationEntity(
    reference = reference,
    interestType = interestType,
    interestTypeOther = interestTypeOther,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )
