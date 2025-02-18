package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepStatus.NOT_STARTED
import java.time.Instant
import java.util.UUID

fun aValidStepEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  title: String = "Book communication skills course",
  status: StepStatus = NOT_STARTED,
  sequenceNumber: Int = 1,
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "a.user.id",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "another.user.id",
): StepEntity = StepEntity(
  reference = reference,
  title = title,
  status = status,
  sequenceNumber = sequenceNumber,
).apply {
  this.id = id
  this.createdAt = createdAt
  this.createdBy = createdBy
  this.updatedAt = updatedAt
  this.updatedBy = updatedBy
}
