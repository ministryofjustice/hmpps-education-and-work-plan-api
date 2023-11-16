package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdatePreviousWorkRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import java.util.UUID

fun aValidCreatePreviousWorkRequest(
  hasWorkedBefore: Boolean = true,
  typeOfWorkExperience: Set<WorkType>? = setOf(WorkType.OTHER),
  typeOfWorkExperienceOther: String? = "Scientist",
  workExperience: Set<WorkExperience>? = setOf(aValidWorkExperienceResource()),
  workInterests: CreateWorkInterestsRequest? = aValidCreateWorkInterestsRequest(),
): CreatePreviousWorkRequest =
  CreatePreviousWorkRequest(
    hasWorkedBefore = hasWorkedBefore,
    typeOfWorkExperience = typeOfWorkExperience,
    typeOfWorkExperienceOther = typeOfWorkExperienceOther,
    workExperience = workExperience,
    workInterests = workInterests,
  )

fun aValidUpdatePreviousWorkRequest(
  id: UUID? = UUID.randomUUID(),
  hasWorkedBefore: Boolean = true,
  typeOfWorkExperience: Set<WorkType>? = setOf(WorkType.OTHER),
  typeOfWorkExperienceOther: String? = "Scientist",
  workExperience: Set<WorkExperience>? = setOf(aValidWorkExperienceResource()),
  workInterests: UpdateWorkInterestsRequest? = aValidUpdateWorkInterestsRequest(),
): UpdatePreviousWorkRequest =
  UpdatePreviousWorkRequest(
    id = id,
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
