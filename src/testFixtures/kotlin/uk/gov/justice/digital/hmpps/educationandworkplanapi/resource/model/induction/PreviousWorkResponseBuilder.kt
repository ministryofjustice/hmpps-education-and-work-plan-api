package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkExperience
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import java.time.OffsetDateTime
import java.util.UUID

fun aValidPreviousWorkResponse(
  id: UUID? = UUID.randomUUID(),
  hasWorkedBefore: Boolean = true,
  typeOfWorkExperience: Set<WorkType>? = setOf(WorkType.OTHER),
  typeOfWorkExperienceOther: String? = "Scientist",
  workExperience: Set<WorkExperience>? = setOf(aValidWorkExperienceResource()),
  workInterests: WorkInterestsResponse? = aValidWorkInterestsResponse(),
  modifiedBy: String = "auser_gen",
  modifiedDateTime: OffsetDateTime = OffsetDateTime.now(),
): PreviousWorkResponse =
  PreviousWorkResponse(
    id = id,
    hasWorkedBefore = hasWorkedBefore,
    typeOfWorkExperience = typeOfWorkExperience,
    typeOfWorkExperienceOther = typeOfWorkExperienceOther,
    workExperience = workExperience,
    workInterests = workInterests,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
