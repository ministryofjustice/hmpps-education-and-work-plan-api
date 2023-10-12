package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HighestEducationLevel.SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS

fun aValidPreviousQualifications(
  educationLevel: HighestEducationLevel = SECONDARY_SCHOOL_LEFT_BEFORE_TAKING_EXAMS,
  qualifications: List<Qualification> = listOf(aValidQualification()),
) =
  PreviousQualifications(
    educationLevel = educationLevel,
    qualifications = qualifications,
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
