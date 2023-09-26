package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TargetDateRange.ZERO_TO_THREE_MONTHS
import java.time.Instant
import java.util.UUID

fun aValidStepEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  targetDateRange: TargetDateRange? = ZERO_TO_THREE_MONTHS,
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "a.user.id",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "another.user.id",
): StepEntity =
  StepEntity(
    id = id,
    reference = reference,
    title = title,
    targetDateRange = targetDateRange,
    status = status,
    sequenceNumber = sequenceNumber,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )
