package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.NOT_STARTED

fun aValidCreateStepDto(
  title: String = "Book communication skills course",
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
): CreateStepDto = CreateStepDto(
  title = title,
  status = status,
  sequenceNumber = sequenceNumber,
)

fun anotherValidCreateStepDto(
  title: String = "Complete communication skills course",
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): CreateStepDto = CreateStepDto(
  title = title,
  status = status,
  sequenceNumber = sequenceNumber,
)
