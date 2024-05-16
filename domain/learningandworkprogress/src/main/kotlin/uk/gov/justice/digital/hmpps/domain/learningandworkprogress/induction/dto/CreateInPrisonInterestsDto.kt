package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonTrainingInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InPrisonWorkInterest

data class CreateInPrisonInterestsDto(
  val inPrisonWorkInterests: List<InPrisonWorkInterest>,
  val inPrisonTrainingInterests: List<InPrisonTrainingInterest>,
  val prisonId: String,
)
