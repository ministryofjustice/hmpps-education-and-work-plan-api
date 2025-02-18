package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel
import java.time.OffsetDateTime
import java.util.UUID

fun aValidAchievedQualificationResponse(
  reference: UUID = UUID.randomUUID(),
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "A",
  createdBy: String = "asmith_gen",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  createdAtPrison: String = "BXI",
  updatedBy: String = "asmith_gen",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedAtPrison: String = "BXI",
): AchievedQualificationResponse = AchievedQualificationResponse(
  reference = reference,
  subject = subject,
  level = level,
  grade = grade,
  createdBy = createdBy,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  updatedBy = updatedBy,
  updatedAt = updatedAt,
  updatedAtPrison = updatedAtPrison,
)

fun anotherValidAchievedQualificationResponse(
  reference: UUID = UUID.randomUUID(),
  subject: String = "Maths",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "B",
  createdBy: String = "asmith_gen",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  updatedBy: String = "asmith_gen",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
): AchievedQualificationResponse = aValidAchievedQualificationResponse(
  reference = reference,
  subject = subject,
  level = level,
  grade = grade,
  createdBy = createdBy,
  createdAt = createdAt,
  updatedBy = updatedBy,
  updatedAt = updatedAt,
)
