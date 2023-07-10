package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange.THREE_TO_SIX_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange.ZERO_TO_THREE_MONTHS
import java.util.*

fun aValidStepResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  targetDateRange: TargetDateRange = ZERO_TO_THREE_MONTHS,
  status: StepStatus = StepStatus.NOT_STARTED,
  sequenceNumber: Int = 1,
): StepResponse =
  StepResponse(
    stepReference = reference.toString(),
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidStepResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  targetDateRange: TargetDateRange = THREE_TO_SIX_MONTHS,
  status: StepStatus = StepStatus.NOT_STARTED,
  sequenceNumber: Int = 2,
): StepResponse =
  StepResponse(
    stepReference = reference.toString(),
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )
