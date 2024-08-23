package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateOrUpdateAchievedQualificationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousQualificationsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel
import java.util.UUID

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
  reference: UUID? = null,
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "A",
): CreateOrUpdateAchievedQualificationRequest = CreateOrUpdateAchievedQualificationRequest(
  reference = reference,
  subject = subject,
  level = level,
  grade = grade,
)

fun anotherValidAchievedQualification(
  reference: UUID? = null,
  subject: String = "Maths",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "B",
): CreateOrUpdateAchievedQualificationRequest = CreateOrUpdateAchievedQualificationRequest(
  reference = reference,
  subject = subject,
  level = level,
  grade = grade,
)
