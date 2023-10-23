package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateStepRequest
import java.util.UUID

fun aValidUpdateStepRequest(
  stepReference: UUID? = UUID.randomUUID(),
  title: String = "Book French course",
  status: StepStatus = StepStatus.ACTIVE,
  sequenceNumber: Int = 1,
): UpdateStepRequest =
  UpdateStepRequest(
    stepReference = stepReference,
    title = title,
    status = status,
    sequenceNumber = sequenceNumber,
  )
