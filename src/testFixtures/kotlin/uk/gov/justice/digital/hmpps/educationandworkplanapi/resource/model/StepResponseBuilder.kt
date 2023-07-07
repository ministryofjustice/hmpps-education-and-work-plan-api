package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate
import java.util.UUID

fun aValidStepResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  targetDate: LocalDate? = LocalDate.now().plusMonths(1),
  status: StepStatus = StepStatus.NOT_STARTED,
  sequenceNumber: Int = 1,
): StepResponse =
  StepResponse(
    stepReference = reference.toString(),
    title = title,
    targetDate = targetDate,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidStepResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  targetDate: LocalDate? = LocalDate.now().plusMonths(6),
  status: StepStatus = StepStatus.NOT_STARTED,
  sequenceNumber: Int = 2,
): StepResponse =
  StepResponse(
    stepReference = reference.toString(),
    title = title,
    targetDate = targetDate,
    status = status,
    sequenceNumber = sequenceNumber,
  )
