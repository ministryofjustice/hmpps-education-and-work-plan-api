package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import java.util.UUID

fun aValidStepResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  status: StepStatus = StepStatus.NOT_STARTED,
  sequenceNumber: Int = 1,
): StepResponse =
  StepResponse(
    stepReference = reference,
    title = title,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidStepResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  status: StepStatus = StepStatus.NOT_STARTED,
  sequenceNumber: Int = 2,
): StepResponse =
  StepResponse(
    stepReference = reference,
    title = title,
    status = status,
    sequenceNumber = sequenceNumber,
  )
