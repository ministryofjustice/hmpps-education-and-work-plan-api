package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.NOT_STARTED
import java.util.UUID

fun aValidUpdateStepDto(
  reference: UUID? = UUID.randomUUID(),
  title: String = "Book communication skills course",
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
): UpdateStepDto = UpdateStepDto(
  reference = reference,
  title = title,
  status = status,
  sequenceNumber = sequenceNumber,
)

fun anotherValidUpdateStepDto(
  reference: UUID? = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): UpdateStepDto = UpdateStepDto(
  reference = reference,
  title = title,
  status = status,
  sequenceNumber = sequenceNumber,
)

fun aValidUpdateStepDtoWithNoReference(
  title: String = "Complete communication skills course",
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): UpdateStepDto = UpdateStepDto(
  reference = null,
  title = title,
  status = status,
  sequenceNumber = sequenceNumber,
)
