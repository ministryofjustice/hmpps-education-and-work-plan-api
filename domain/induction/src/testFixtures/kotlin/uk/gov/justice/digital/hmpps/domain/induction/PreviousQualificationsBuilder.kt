package uk.gov.justice.digital.hmpps.domain.induction

import uk.gov.justice.digital.hmpps.domain.induction.HighestEducationLevel.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
import java.time.Instant
import java.util.UUID

fun aValidPreviousQualifications(
  reference: UUID = UUID.randomUUID(),
  educationLevel: HighestEducationLevel? = SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  qualifications: List<Qualification> = listOf(aValidQualification()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) =
  PreviousQualifications(
    reference = reference,
    educationLevel = educationLevel,
    qualifications = qualifications,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
  )

fun aValidQualification(
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_1,
  grade: String = "C",
) =
  Qualification(
    subject = subject,
    level = level,
    grade = grade,
  )
