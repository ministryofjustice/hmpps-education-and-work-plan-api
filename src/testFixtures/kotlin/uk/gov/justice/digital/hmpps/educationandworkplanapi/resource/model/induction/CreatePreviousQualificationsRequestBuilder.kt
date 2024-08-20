package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateOrUpdateAchievedQualificationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel

fun aValidCreatePreviousQualificationsRequest(
  educationLevel: EducationLevel? = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: List<CreateOrUpdateAchievedQualificationRequest>? = listOf(
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
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "A",
): CreateOrUpdateAchievedQualificationRequest = CreateOrUpdateAchievedQualificationRequest(
  subject = subject,
  level = level,
  grade = grade,
)

fun anotherValidAchievedQualification(
  subject: String = "Maths",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "B",
): CreateOrUpdateAchievedQualificationRequest = CreateOrUpdateAchievedQualificationRequest(
  subject = subject,
  level = level,
  grade = grade,
)
