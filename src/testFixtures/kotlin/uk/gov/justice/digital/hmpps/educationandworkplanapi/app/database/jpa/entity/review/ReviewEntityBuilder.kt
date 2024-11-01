package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidReviewEntity(
  id: UUID = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = aValidPrisonNumber(),
  deadlineDate: LocalDate = LocalDate.now().plusMonths(1),
  completedDate: LocalDate = LocalDate.now(),
  conductedBy: String? = "Barnie Jones",
  conductedByRole: String? = "Peer mentor",
  createdBy: String = "asmith_gen",
  createdAt: Instant = Instant.now(),
  createdAtPrison: String = "BXI",
  updatedAt: Instant = Instant.now(),
  updatedBy: String = "bjones_gen",
  updatedAtPrison: String = "BXI",
): ReviewEntity =
  ReviewEntity(
    reference = reference,
    prisonNumber = prisonNumber,
    deadlineDate = deadlineDate,
    completedDate = completedDate,
    conductedBy = conductedBy,
    conductedByRole = conductedByRole,
    createdAtPrison = createdAtPrison,
    updatedAtPrison = updatedAtPrison,
  ).apply {
    this.id = id
    this.createdAt = createdAt
    this.createdBy = createdBy
    this.updatedAt = updatedAt
    this.updatedBy = updatedBy
  }
