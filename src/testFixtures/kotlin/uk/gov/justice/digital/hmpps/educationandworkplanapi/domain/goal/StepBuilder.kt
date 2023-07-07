package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.NOT_STARTED
import java.time.LocalDate
import java.util.UUID

fun aValidStep(
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  targetDate: LocalDate? = LocalDate.now().plusMonths(1),
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
): Step =
  Step(
    reference = reference,
    title = title,
    targetDate = targetDate,
    status = status,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidStep(
  reference: UUID = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  targetDate: LocalDate? = LocalDate.now().plusMonths(6),
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): Step =
  Step(
    reference = reference,
    title = title,
    targetDate = targetDate,
    status = status,
    sequenceNumber = sequenceNumber,
  )
