package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import java.time.OffsetDateTime
import java.util.UUID

fun aValidCreateWorkInterestsRequest(
  workInterests: Set<WorkType>? = setOf(WorkType.OTHER),
  workInterestsOther: String? = "Any job I can get",
  particularJobInterests: Set<WorkInterestDetail>? = setOf(
    WorkInterestDetail(
      workInterest = WorkType.OTHER,
      role = "Any role",
    ),
  ),
): CreateWorkInterestsRequest =
  CreateWorkInterestsRequest(
    workInterests = workInterests,
    workInterestsOther = workInterestsOther,
    particularJobInterests = particularJobInterests,
  )

fun aValidUpdateWorkInterestsRequest(
  id: UUID = UUID.randomUUID(),
  workInterests: Set<WorkType>? = setOf(WorkType.OTHER),
  workInterestsOther: String? = "Any job I can get",
  particularJobInterests: Set<WorkInterestDetail>? = setOf(
    WorkInterestDetail(
      workInterest = WorkType.OTHER,
      role = "Any role",
    ),
  ),
): UpdateWorkInterestsRequest =
  UpdateWorkInterestsRequest(
    id = id,
    workInterests = workInterests,
    workInterestsOther = workInterestsOther,
    particularJobInterests = particularJobInterests,
  )

fun aValidWorkInterestsResponse(
  id: UUID = UUID.randomUUID(),
  workInterests: Set<WorkType> = setOf(WorkType.OTHER),
  workInterestsOther: String? = "Any job I can get",
  particularJobInterests: Set<WorkInterestDetail>? = setOf(
    WorkInterestDetail(
      workInterest = WorkType.OTHER,
      role = "Any role",
    ),
  ),
  modifiedBy: String = "auser_gen",
  modifiedDateTime: OffsetDateTime = OffsetDateTime.now(),
): WorkInterestsResponse =
  WorkInterestsResponse(
    id = id,
    workInterests = workInterests,
    workInterestsOther = workInterestsOther,
    particularJobInterests = particularJobInterests,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
