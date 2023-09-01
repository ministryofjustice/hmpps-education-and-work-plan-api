package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate
import java.util.UUID

fun aValidActionPlanSummaryResponse(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
): ActionPlanSummaryResponse =
  ActionPlanSummaryResponse(
    reference = reference,
    prisonNumber = prisonNumber,
    reviewDate = reviewDate,
  )
