package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

import java.time.Instant
import java.util.UUID

fun aValidPreviousQualificationsMigrationEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  educationLevel: HighestEducationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: MutableList<QualificationMigrationEntity> = mutableListOf(aValidQualificationMigrationEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  PreviousQualificationsMigrationEntity(
    id = id,
    reference = reference,
    educationLevel = educationLevel,
    qualifications = qualifications,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    updatedAt = updatedAt,
    updatedAtPrison = updatedAtPrison,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )

fun aValidQualificationMigrationEntity(
  reference: UUID = UUID.randomUUID(),
  subject: String = "English",
  level: QualificationLevel? = QualificationLevel.LEVEL_3,
  grade: String? = "A",
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "bjones_gen",
) =
  QualificationMigrationEntity(
    reference = reference,
    subject = subject,
    level = level,
    grade = grade,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )
