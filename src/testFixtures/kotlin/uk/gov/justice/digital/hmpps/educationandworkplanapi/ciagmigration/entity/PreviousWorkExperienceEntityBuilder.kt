package uk.gov.justice.digital.hmpps.educationandworkplanapi.ciagmigration.entity

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction.PreviousWorkExperiencesMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction.WorkExperienceMigrationEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction.WorkExperienceType
import java.time.Instant
import java.util.UUID

fun aValidPreviousWorkExperiencesMigrationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
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
  experienceTypeOther: String? = "Warehouse work",
  role: String? = "Chief Forklift Truck Driver",
  details: String? = "Forward, pick stuff up, reverse etc",
) =
  WorkExperienceMigrationEntity(
    reference = reference,
    experienceType = experienceType,
    experienceTypeOther = experienceTypeOther,
    role = role,
    details = details,
  )
