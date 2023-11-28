package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

import java.time.Instant
import java.util.UUID

fun aValidPreviousWorkExperiencesMigrationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  hasWorkedBefore: Boolean = true,
  experiences: MutableList<WorkExperienceMigrationEntity> = mutableListOf(aValidWorkExperienceMigrationEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  PreviousWorkExperiencesMigrationEntity(
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

fun aValidWorkExperienceMigrationEntity(
  reference: UUID = UUID.randomUUID(),
  experienceType: WorkExperienceType = WorkExperienceType.OTHER,
  experienceTypeOther: String? = "Scientist",
  role: String? = "Lab Technician",
  details: String? = "Cleaning test tubes",
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
) =
  WorkExperienceMigrationEntity(
    reference = reference,
    experienceType = experienceType,
    experienceTypeOther = experienceTypeOther,
    role = role,
    details = details,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )
