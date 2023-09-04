package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.THREE_TO_SIX_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS

fun aValidCreateStepDto(
  title: String = "Book communication skills course",
  targetDateRange: TargetDateRange = ZERO_TO_THREE_MONTHS,
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
): CreateStepDto =
  CreateStepDto(
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidCreateStepDto(
  title: String = "Complete communication skills course",
  targetDateRange: TargetDateRange = THREE_TO_SIX_MONTHS,
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): CreateStepDto =
  CreateStepDto(
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )
