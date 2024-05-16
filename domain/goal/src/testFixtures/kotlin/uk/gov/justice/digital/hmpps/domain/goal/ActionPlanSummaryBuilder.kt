package uk.gov.justice.digital.hmpps.domain.goal

import java.time.LocalDate
import java.util.UUID

fun aValidActionPlanSummary(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
): ActionPlanSummary =
  ActionPlanSummary(
    reference = reference,
    prisonNumber = prisonNumber,
    reviewDate = reviewDate,
  )
