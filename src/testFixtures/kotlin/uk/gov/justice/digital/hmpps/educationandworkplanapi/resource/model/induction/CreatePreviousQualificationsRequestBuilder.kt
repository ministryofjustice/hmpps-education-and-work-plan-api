package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HighestEducationLevel

fun aValidCreatePreviousQualificationsRequest(
  educationLevel: HighestEducationLevel? = HighestEducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: List<AchievedQualification>? = listOf(
    aValidAchievedQualification(),
    anotherValidAchievedQualification(),
  ),
): CreatePreviousQualificationsRequest =
  CreatePreviousQualificationsRequest(
    educationLevel = educationLevel,
    qualifications = qualifications,
  )

fun aValidAchievedQualification(
  subject: String = "English",
  level: AchievedQualification.Level = AchievedQualification.Level.LEVEL_3,
  grade: String = "A",
): AchievedQualification = AchievedQualification(
  subject = subject,
  level = level,
  grade = grade,
)

fun anotherValidAchievedQualification(
  subject: String = "Maths",
  level: AchievedQualification.Level = AchievedQualification.Level.LEVEL_3,
  grade: String = "B",
): AchievedQualification = AchievedQualification(
  subject = subject,
  level = level,
  grade = grade,
)
