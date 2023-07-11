package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.THREE_TO_SIX_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS
import java.util.UUID

fun aValidStep(
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  targetDateRange: TargetDateRange = ZERO_TO_THREE_MONTHS,
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
): Step =
  Step(
    reference = reference,
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidStep(
  reference: UUID = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  targetDateRange: TargetDateRange = THREE_TO_SIX_MONTHS,
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): Step =
  Step(
    reference = reference,
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
  )
