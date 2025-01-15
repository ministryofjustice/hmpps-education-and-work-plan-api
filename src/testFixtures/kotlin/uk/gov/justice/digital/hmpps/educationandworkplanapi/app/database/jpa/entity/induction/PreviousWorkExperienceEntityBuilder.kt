package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidPreviousWorkExperiencesEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  hasWorkedBefore: HasWorkedBefore = HasWorkedBefore.YES,
  hasWorkedBeforeNotRelevantReason: String? = null,
  experiences: MutableList<WorkExperienceEntity> = mutableListOf(aValidWorkExperienceEntity()),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
) =
  PreviousWorkExperiencesEntity(
    reference = reference,
    hasWorkedBefore = hasWorkedBefore,
    hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
    experiences = experiences,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  ).apply {
    this.id = id
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }

fun aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  hasWorkedBefore: HasWorkedBefore = HasWorkedBefore.YES,
  experiences: MutableList<WorkExperienceEntity> = mutableListOf(aValidWorkExperienceEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
) =
  PreviousWorkExperiencesEntity(
    reference = reference,
    hasWorkedBefore = hasWorkedBefore,
    experiences = experiences,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  ).apply {
    this.id = id
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }

fun aValidWorkExperienceEntity(
  reference: UUID = UUID.randomUUID(),
  experienceType: WorkExperienceType = WorkExperienceType.OTHER,
  experienceTypeOther: String? = "Warehouse work",
  role: String = "Chief Forklift Truck Driver",
  details: String = "Forward, pick stuff up, reverse etc",
) =
  WorkExperienceEntity(
    reference = reference,
    experienceType = experienceType,
    experienceTypeOther = experienceTypeOther,
    role = role,
    details = details,
  )
