package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange
import java.util.UUID

/**
 * A DTO class that contains the data required to update an existing Step domain object
 */
data class UpdateStepDto(
  val reference: UUID?,
  val title: String,
  val targetDateRange: TargetDateRange?,
  val status: StepStatus = StepStatus.NOT_STARTED,
  val sequenceNumber: Int,
)
