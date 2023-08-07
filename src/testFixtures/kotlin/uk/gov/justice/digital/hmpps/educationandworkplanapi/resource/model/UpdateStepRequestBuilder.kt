package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.util.UUID

fun aValidUpdateStepRequest(
  stepReference: UUID? = UUID.randomUUID(),
  title: String = "Book French course",
  targetDateRange: TargetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
  status: StepStatus = StepStatus.ACTIVE,
  sequenceNumber: Int = 1,
): UpdateStepRequest =
  UpdateStepRequest(
    stepReference = stepReference,
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )
