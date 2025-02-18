package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HasWorkedBefore
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

fun aValidCreatePreviousWorkExperiencesRequest(
  hasWorkedBefore: HasWorkedBefore = HasWorkedBefore.YES,
  hasWorkedBeforeNotRelevantReason: String? = null,
  experiences: List<PreviousWorkExperience>? = listOf(aValidPreviousWorkExperience()),
): CreatePreviousWorkExperiencesRequest = CreatePreviousWorkExperiencesRequest(
  hasWorkedBefore = hasWorkedBefore,
  hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
  experiences = experiences,
)

fun aValidPreviousWorkExperience(
  experienceType: WorkType = WorkType.OTHER,
  experienceTypeOther: String? = "Scientist",
  role: String? = "Lab Technician",
  details: String? = "Cleaning test tubes",
): PreviousWorkExperience = PreviousWorkExperience(
  experienceType = experienceType,
  experienceTypeOther = experienceTypeOther,
  role = role,
  details = details,
)
