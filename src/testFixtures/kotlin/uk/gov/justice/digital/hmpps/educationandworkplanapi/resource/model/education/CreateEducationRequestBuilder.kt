package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateAchievedQualificationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEducationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel

fun aValidCreateEducationRequest(
  prisonId: String = "BXI",
  educationLevel: EducationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: List<CreateAchievedQualificationRequest> = listOf(
    aValidAchievedQualification(),
    anotherValidAchievedQualification(),
  ),
): CreateEducationRequest = CreateEducationRequest(
  prisonId = prisonId,
  educationLevel = educationLevel,
  qualifications = qualifications,
)

fun aValidAchievedQualification(
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "A",
): CreateAchievedQualificationRequest = CreateAchievedQualificationRequest(
  subject = subject,
  level = level,
  grade = grade,
)

fun anotherValidAchievedQualification(
  subject: String = "Maths",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "B",
): CreateAchievedQualificationRequest = CreateAchievedQualificationRequest(
  subject = subject,
  level = level,
  grade = grade,
)
