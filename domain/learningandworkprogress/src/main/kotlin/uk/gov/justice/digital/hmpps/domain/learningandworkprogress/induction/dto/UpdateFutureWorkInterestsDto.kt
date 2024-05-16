package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterest
import java.util.UUID

data class UpdateFutureWorkInterestsDto(
  val reference: UUID?,
  val interests: List<WorkInterest>,
  val prisonId: String,
)
