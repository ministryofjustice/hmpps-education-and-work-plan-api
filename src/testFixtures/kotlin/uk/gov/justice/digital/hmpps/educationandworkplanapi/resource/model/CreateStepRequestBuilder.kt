package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange.THREE_TO_SIX_MONTHS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TargetDateRange.ZERO_TO_THREE_MONTHS
import java.time.LocalDate

fun aValidCreateStepRequest(
  title: String = "Book communication skills course",
  targetDateRange: TargetDateRange? = ZERO_TO_THREE_MONTHS,
  targetDate: LocalDate? = null,
  sequenceNumber: Int = 1,
): CreateStepRequest =
  CreateStepRequest(
    title = title,
    targetDateRange = targetDateRange,
    targetDate = targetDate,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidCreateStepRequest(
  title: String = "Complete communication skills course",
  targetDateRange: TargetDateRange? = THREE_TO_SIX_MONTHS,
  targetDate: LocalDate? = null,
  sequenceNumber: Int = 2,
): CreateStepRequest =
  CreateStepRequest(
    title = title,
    targetDateRange = targetDateRange,
    targetDate = targetDate,
    sequenceNumber = sequenceNumber,
  )
