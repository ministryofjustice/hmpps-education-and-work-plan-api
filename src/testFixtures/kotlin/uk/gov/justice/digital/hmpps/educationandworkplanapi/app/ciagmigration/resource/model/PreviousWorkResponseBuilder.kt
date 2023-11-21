package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import java.time.LocalDateTime

fun aValidPreviousWorkResponse(
  hasWorkedBefore: Boolean = true,
  typeOfWorkExperience: Set<WorkType>? = setOf(WorkType.OTHER),
  typeOfWorkExperienceOther: String? = "Scientist",
  workExperience: Set<WorkExperience>? = setOf(aValidWorkExperienceResource()),
  workInterests: WorkInterestsResponse? = aValidWorkInterestsResponse(),
  modifiedBy: String = "auser_gen",
  modifiedDateTime: LocalDateTime = LocalDateTime.now(),
): PreviousWorkResponse =
  PreviousWorkResponse(
    hasWorkedBefore = hasWorkedBefore,
    typeOfWorkExperience = typeOfWorkExperience,
    typeOfWorkExperienceOther = typeOfWorkExperienceOther,
    workExperience = workExperience,
    workInterests = workInterests,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
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
