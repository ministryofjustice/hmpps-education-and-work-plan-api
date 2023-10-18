package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import java.util.UUID

fun aValidPreviousQualificationsEntity(
  reference: UUID = UUID.randomUUID(),
  educationLevel: HighestEducationLevel = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: List<QualificationEntity> = listOf(aValidQualificationEntity()),
  createdAtPrison: String = "BXI",
  updatedAtPrison: String = "BXI",
) =
  PreviousQualificationsEntity(
    reference = reference,
    educationLevel = educationLevel,
    qualifications = qualifications,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
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
