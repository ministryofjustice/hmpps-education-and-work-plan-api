package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.Instant
import java.util.UUID

fun aValidPreviousQualificationsEntity(
  id: UUID? = null,
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  educationLevel: EducationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: MutableList<QualificationEntity> = mutableListOf(aValidQualificationEntity()),
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
) =
  PreviousQualificationsEntity(
    reference = reference,
    prisonNumber = prisonNumber,
    educationLevel = educationLevel,
    qualifications = qualifications,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  ).apply {
    this.id = id
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }

fun aValidPreviousQualificationsEntityWithJpaFieldsPopulated(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  educationLevel: EducationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: MutableList<QualificationEntity> = mutableListOf(aValidQualificationEntity()),
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  createdBy: String? = "asmith_gen",
  updatedAt: Instant? = Instant.now(),
  updatedAtPrison: String = "BXI",
  updatedBy: String? = "bjones_gen",
) =
  PreviousQualificationsEntity(
    reference = reference,
    prisonNumber = prisonNumber,
    educationLevel = educationLevel,
    qualifications = qualifications,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  ).apply {
    this.id = id
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }

fun aValidQualificationEntity(
  reference: UUID = UUID.randomUUID(),
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "A",
  createdAt: Instant? = null,
  createdAtPrison: String = "BXI",
  createdBy: String? = null,
  updatedAt: Instant? = null,
  updatedAtPrison: String = "BXI",
  updatedBy: String? = null,
) =
  QualificationEntity(
    reference = reference,
    subject = subject,
    level = level,
    grade = grade,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  ).apply {
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }
