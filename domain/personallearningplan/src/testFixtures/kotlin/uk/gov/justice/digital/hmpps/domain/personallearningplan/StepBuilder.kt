package uk.gov.justice.digital.hmpps.domain.personallearningplan

import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.NOT_STARTED
import java.util.UUID

fun aValidStep(
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
): Step = Step(
  reference = reference,
  title = title,
  status = status,
  sequenceNumber = sequenceNumber,
)

fun anotherValidStep(
  reference: UUID = UUID.randomUUID(),
  title: String = "Complete communication skills course",
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 2,
): Step = Step(
  reference = reference,
  title = title,
  status = status,
  sequenceNumber = sequenceNumber,
)
