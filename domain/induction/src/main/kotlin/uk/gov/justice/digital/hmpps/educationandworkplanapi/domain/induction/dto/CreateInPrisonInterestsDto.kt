package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InPrisonWorkInterest

data class CreateInPrisonInterestsDto(
  val inPrisonWorkInterests: List<InPrisonWorkInterest>,
  val inPrisonTrainingInterests: List<InPrisonTrainingInterest>,
  val prisonId: String,
)
