package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateWorkInterestsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType
import java.util.UUID

fun aValidCreateWorkInterestsRequest(
  workInterests: Set<WorkType> = setOf(WorkType.OTHER),
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

fun aValidWorkInterests(
  id: UUID = UUID.randomUUID(),
  workInterests: Set<WorkType> = setOf(WorkType.OTHER),
  workInterestsOther: String? = "Any job I can get",
  particularJobInterests: Set<WorkInterestDetail>? = setOf(
    WorkInterestDetail(
      workInterest = WorkType.OTHER,
      role = "Any role",
    ),
  ),
): WorkInterests =
  WorkInterests(
    id = id,
    workInterests = workInterests,
    workInterestsOther = workInterestsOther,
    particularJobInterests = particularJobInterests,
  )
