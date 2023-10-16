package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HighestEducationLevel.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS
import java.time.Instant

fun aValidPreviousQualifications(
  educationLevel: HighestEducationLevel = SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  qualifications: List<Qualification> = listOf(aValidQualification()),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
) =
  PreviousQualifications(
    educationLevel = educationLevel,
    qualifications = qualifications,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
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
