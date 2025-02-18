package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationLevel.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
import java.time.Instant
import java.util.UUID

fun aValidPreviousQualifications(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  educationLevel: EducationLevel = SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  qualifications: List<Qualification> = listOf(aValidQualification()),
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  PreviousQualifications(
    reference = reference,
    prisonNumber = prisonNumber,
    educationLevel = educationLevel,
    qualifications = qualifications,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )

fun aValidQualification(
  reference: UUID = UUID.randomUUID(),
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_1,
  grade: String = "C",
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String = "bjones_gen",
  lastUpdatedAt: Instant = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  Qualification(
    reference = reference,
    subject = subject,
    level = level,
    grade = grade,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )
