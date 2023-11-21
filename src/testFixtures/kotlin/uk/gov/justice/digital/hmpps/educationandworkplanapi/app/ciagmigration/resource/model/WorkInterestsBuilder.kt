package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.resource.model

import java.time.LocalDateTime

fun aValidWorkInterestsResponse(
  workInterests: Set<WorkType> = setOf(WorkType.OTHER),
  workInterestsOther: String? = "Any job I can get",
  particularJobInterests: Set<WorkInterestDetail>? = setOf(
    WorkInterestDetail(
      workInterest = WorkType.OTHER,
      role = "Any role",
    ),
  ),
  modifiedBy: String = "auser_gen",
  modifiedDateTime: LocalDateTime = LocalDateTime.now(),
): WorkInterestsResponse =
  WorkInterestsResponse(
    workInterests = workInterests,
    workInterestsOther = workInterestsOther,
    particularJobInterests = particularJobInterests,
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime,
  )
