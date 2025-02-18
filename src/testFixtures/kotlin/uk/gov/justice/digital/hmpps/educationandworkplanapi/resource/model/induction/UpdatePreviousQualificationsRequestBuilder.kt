package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateOrUpdateAchievedQualificationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousQualificationsRequest
import java.util.UUID

fun aValidUpdatePreviousQualificationsRequest(
  reference: UUID? = UUID.randomUUID(),
  educationLevel: EducationLevel? = EducationLevel.SECONDARY_SCHOOL_TOOK_EXAMS,
  qualifications: List<CreateOrUpdateAchievedQualificationRequest>? = listOf(
    aValidAchievedQualification(),
    anotherValidAchievedQualification(),
  ),
): UpdatePreviousQualificationsRequest = UpdatePreviousQualificationsRequest(
  reference = reference,
  educationLevel = educationLevel,
  qualifications = qualifications,
)
