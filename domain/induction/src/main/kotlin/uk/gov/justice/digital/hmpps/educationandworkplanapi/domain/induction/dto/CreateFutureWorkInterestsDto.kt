package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.WorkInterest

data class CreateFutureWorkInterestsDto(
  val interests: List<WorkInterest>,
  val prisonId: String,
)
