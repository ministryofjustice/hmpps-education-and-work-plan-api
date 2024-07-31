package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.Instant
import java.util.UUID

fun aValidPreviousQualificationsEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  educationLevel: HighestEducationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: MutableList<QualificationEntity> = mutableListOf(aValidQualificationEntity()),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  createdByDisplayName: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
  updatedByDisplayName: String? = null,
) =
  PreviousQualificationsEntity(
    id = id,
    reference = reference,
    prisonNumber = prisonNumber,
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

fun aValidPreviousQualificationsEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  educationLevel: HighestEducationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: MutableList<QualificationEntity> = mutableListOf(aValidQualificationEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
  updatedByDisplayName: String? = "Barry Jones",
) =
  PreviousQualificationsEntity(
    id = id,
    reference = reference,
    prisonNumber = prisonNumber,
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

fun aValidQualificationEntity(
  reference: UUID = UUID.randomUUID(),
  subject: String = "English",
  level: QualificationLevel? = QualificationLevel.LEVEL_3,
  grade: String? = "A",
) =
  QualificationEntity(
    reference = reference,
    subject = subject,
    level = level,
    grade = grade,
  )
