package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.THREE_TO_SIX_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS
import java.util.UUID

fun aValidUpdateStepDto(
  reference: UUID? = UUID.randomUUID(),
  title: String = "Book communication skills course",
  targetDateRange: TargetDateRange = ZERO_TO_THREE_MONTHS,
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
): UpdateStepDto =
  UpdateStepDto(
    reference = reference,
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidUpdateStepDto(
  reference: UUID? = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  targetDateRange: TargetDateRange = THREE_TO_SIX_MONTHS,
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): UpdateStepDto =
  UpdateStepDto(
    reference = reference,
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun aValidUpdateStepDtoWithNoReference(
  title: String = "Complete communication skills course",
  targetDateRange: TargetDateRange = THREE_TO_SIX_MONTHS,
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): UpdateStepDto =
  UpdateStepDto(
    reference = null,
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )
