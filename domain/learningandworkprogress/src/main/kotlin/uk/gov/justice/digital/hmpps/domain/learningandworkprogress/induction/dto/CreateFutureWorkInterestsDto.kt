package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterest

data class CreateFutureWorkInterestsDto(
  val interests: List<WorkInterest>,
  val prisonId: String,
)
