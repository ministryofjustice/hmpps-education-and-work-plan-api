package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.time.Instant
import java.util.UUID

fun aValidPreviousWorkExperiencesEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  hasWorkedBefore: Boolean = true,
  experiences: MutableList<WorkExperienceEntity> = mutableListOf(aValidWorkExperienceEntity()),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  createdByDisplayName: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
  updatedByDisplayName: String? = null,
) =
  PreviousWorkExperiencesEntity(
    id = id,
    reference = reference,
    hasWorkedBefore = hasWorkedBefore,
    experiences = experiences,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )

fun aValidPreviousWorkExperiencesEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  hasWorkedBefore: Boolean = true,
  experiences: MutableList<WorkExperienceEntity> = mutableListOf(aValidWorkExperienceEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  PreviousWorkExperiencesEntity(
    id = id,
    reference = reference,
    hasWorkedBefore = hasWorkedBefore,
    experiences = experiences,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )

fun aValidWorkExperienceEntity(
  reference: UUID = UUID.randomUUID(),
  experienceType: WorkExperienceType = WorkExperienceType.OTHER,
  experienceTypeOther: String? = "Warehouse work",
  role: String? = "Chief Forklift Truck Driver",
  details: String? = "Forward, pick stuff up, reverse etc",
) =
  WorkExperienceEntity(
    reference = reference,
    experienceType = experienceType,
    experienceTypeOther = experienceTypeOther,
    role = role,
    details = details,
  )
