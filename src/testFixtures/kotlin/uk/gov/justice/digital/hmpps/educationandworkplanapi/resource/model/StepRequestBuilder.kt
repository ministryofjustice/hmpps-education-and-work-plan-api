package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange.THREE_TO_SIX_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange.ZERO_TO_THREE_MONTHS
import java.util.UUID

fun aValidCreateStepRequest(
  title: String = "Book communication skills course",
  targetDateRange: TargetDateRange = ZERO_TO_THREE_MONTHS,
  sequenceNumber: Int = 1,
): StepRequest =
  StepRequest(
    title = title,
    targetDateRange = targetDateRange,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidCreateStepRequest(
  title: String = "Complete communication skills course",
  targetDateRange: TargetDateRange = THREE_TO_SIX_MONTHS,
  sequenceNumber: Int = 2,
): StepRequest =
  StepRequest(
    title = title,
    targetDateRange = targetDateRange,
    sequenceNumber = sequenceNumber,
  )

fun aValidUpdateStepRequest(
  stepReference: UUID = UUID.randomUUID(),
  title: String = "Book French course",
  targetDateRange: TargetDateRange = ZERO_TO_THREE_MONTHS,
  sequenceNumber: Int = 1,
): StepRequest =
  StepRequest(
    stepReference = stepReference,
    title = title,
    targetDateRange = targetDateRange,
    sequenceNumber = sequenceNumber,
  )
