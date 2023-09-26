package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange

/**
 * A DTO class that contains the data required to create a new Step domain object
 */
data class CreateStepDto(
  val title: String,
  val targetDateRange: TargetDateRange?,
  val status: StepStatus = StepStatus.NOT_STARTED,
  val sequenceNumber: Int,
)
