package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewRequest
import java.time.LocalDate

fun aValidCreateActionPlanReviewRequest(
  prisonId: String = "BXI",
  note: String = "Note content",
  conductedAt: LocalDate = LocalDate.now(),
  conductedBy: String? = "Barnie Jones",
  conductedByRole: String? = "Peer mentor",
): CreateActionPlanReviewRequest = CreateActionPlanReviewRequest(
  prisonId = prisonId,
  note = note,
  conductedBy = conductedBy,
  conductedByRole = conductedByRole,
  conductedAt = conductedAt,
)
