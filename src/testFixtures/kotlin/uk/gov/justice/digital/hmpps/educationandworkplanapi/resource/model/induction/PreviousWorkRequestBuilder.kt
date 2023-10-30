package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

fun aValidCreatePreviousWorkRequest(
  hasWorkedBefore: Boolean = true,
  typeOfWorkExperience: Set<WorkType>? = setOf(WorkType.OTHER),
  typeOfWorkExperienceOther: String? = "Scientist",
  workExperience: Set<WorkExperience>? = setOf(aValidWorkExperienceResource()),
  workInterests: WorkInterests? = aValidWorkInterests(),
): CreatePreviousWorkRequest =
  CreatePreviousWorkRequest(
    hasWorkedBefore = hasWorkedBefore,
    typeOfWorkExperience = typeOfWorkExperience,
    typeOfWorkExperienceOther = typeOfWorkExperienceOther,
    workExperience = workExperience,
    workInterests = workInterests,
  )

fun aValidWorkExperienceResource(
  typeOfWorkExperience: WorkType = WorkType.OTHER,
  otherWork: String? = "Scientist",
  role: String? = "Lab Technician",
  details: String? = "Cleaning test tubes",
): WorkExperience = WorkExperience(
  typeOfWorkExperience = typeOfWorkExperience,
  otherWork = otherWork,
  role = role,
  details = details,
)
