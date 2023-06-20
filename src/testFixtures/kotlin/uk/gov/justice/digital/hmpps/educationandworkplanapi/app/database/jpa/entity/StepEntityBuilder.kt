package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus.NOT_STARTED
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidStepEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  targetDate: LocalDate = LocalDate.now().plusMonths(1),
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
  createdAt: Instant? = Instant.now(),
  updatedAt: Instant? = Instant.now(),
): StepEntity =
  StepEntity(
    id = id,
    reference = reference,
    title = title,
    targetDate = targetDate,
    status = status,
    sequenceNumber = sequenceNumber,
    createdAt = createdAt,
    updatedAt = updatedAt,
  )
