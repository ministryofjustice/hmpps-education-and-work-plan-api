package uk.gov.justice.digital.hmpps.domain.goal.dto

import uk.gov.justice.digital.hmpps.domain.goal.StepStatus

/**
 * A DTO class that contains the data required to create a new Step domain object
 */
data class CreateStepDto(
  val title: String,
  val status: StepStatus = StepStatus.NOT_STARTED,
  val sequenceNumber: Int,
)
