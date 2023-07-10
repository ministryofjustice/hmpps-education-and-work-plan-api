package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange.THREE_TO_SIX_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange.ZERO_TO_THREE_MONTHS
import java.time.LocalDate
import java.util.UUID

fun aValidStepResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  targetDateRange: TargetDateRange = ZERO_TO_THREE_MONTHS,
  targetDate: LocalDate = LocalDate.now().plusMonths(1),
  status: StepStatus = StepStatus.NOT_STARTED,
  sequenceNumber: Int = 1,
): StepResponse =
  StepResponse(
    stepReference = reference.toString(),
    title = title,
    targetDateRange = targetDateRange,
    targetDate = targetDate,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidStepResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  targetDateRange: TargetDateRange = THREE_TO_SIX_MONTHS,
  targetDate: LocalDate = LocalDate.now().plusMonths(6),
  status: StepStatus = StepStatus.NOT_STARTED,
  sequenceNumber: Int = 2,
): StepResponse =
  StepResponse(
    stepReference = reference.toString(),
    title = title,
    targetDateRange = targetDateRange,
    targetDate = targetDate,
    status = status,
    sequenceNumber = sequenceNumber,
  )
