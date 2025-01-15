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
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
) =
  PersonalSkillsAndInterestsEntity(
    reference = reference,
    skills = skills,
    interests = interests,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  ).apply {
    this.id = id
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }

fun aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  skills: MutableList<PersonalSkillEntity> = mutableListOf(aValidPersonalSkillEntity()),
  interests: MutableList<PersonalInterestEntity> = mutableListOf(aValidPersonalInterestEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
) =
  PersonalSkillsAndInterestsEntity(
    reference = reference,
    skills = skills,
    interests = interests,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  ).apply {
    this.id = id
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }

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
