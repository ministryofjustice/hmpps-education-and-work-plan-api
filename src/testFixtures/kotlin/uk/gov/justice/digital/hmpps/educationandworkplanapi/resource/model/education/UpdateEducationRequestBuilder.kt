package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateOrUpdateAchievedQualificationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateEducationRequest
import java.util.UUID

fun aValidUpdateEducationRequest(
  reference: UUID = UUID.randomUUID(),
  prisonId: String = "BXI",
  educationLevel: EducationLevel = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: List<CreateOrUpdateAchievedQualificationRequest> = listOf(
    aValidAchievedQualificationToCreate(),
    aValidAchievedQualificationToUpdate(),
  ),
): UpdateEducationRequest = UpdateEducationRequest(
  reference = reference,
  prisonId = prisonId,
  educationLevel = educationLevel,
  qualifications = qualifications,
)

fun aValidAchievedQualificationToCreate(
  subject: String = "English",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "A",
): CreateOrUpdateAchievedQualificationRequest = CreateOrUpdateAchievedQualificationRequest(
  subject = subject,
  level = level,
  grade = grade,
)

fun aValidAchievedQualificationToUpdate(
  reference: UUID = UUID.randomUUID(),
  subject: String = "Maths",
  level: QualificationLevel = QualificationLevel.LEVEL_3,
  grade: String = "B",
): CreateOrUpdateAchievedQualificationRequest = CreateOrUpdateAchievedQualificationRequest(
  reference = reference,
  subject = subject,
  level = level,
  grade = grade,
)
