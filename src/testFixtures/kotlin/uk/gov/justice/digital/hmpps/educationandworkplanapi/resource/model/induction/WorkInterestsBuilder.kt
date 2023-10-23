package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterestDetail
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkInterests
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.WorkType

fun aValidWorkInterests(
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
    workInterests = workInterests,
    workInterestsOther = workInterestsOther,
    particularJobInterests = particularJobInterests,
  )
