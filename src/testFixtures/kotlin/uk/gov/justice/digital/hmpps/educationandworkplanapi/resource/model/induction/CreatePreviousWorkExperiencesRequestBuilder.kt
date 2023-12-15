package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkExperiencesRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

fun aValidCreatePreviousWorkExperiencesRequest(
  hasWorkedBefore: Boolean = true,
  experiences: List<PreviousWorkExperience>? = listOf(aValidPreviousWorkExperience()),
): CreatePreviousWorkExperiencesRequest =
  CreatePreviousWorkExperiencesRequest(
    hasWorkedBefore = hasWorkedBefore,
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
