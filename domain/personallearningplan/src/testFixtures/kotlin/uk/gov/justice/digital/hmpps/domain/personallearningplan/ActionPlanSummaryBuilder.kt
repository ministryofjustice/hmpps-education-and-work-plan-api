package uk.gov.justice.digital.hmpps.domain.personallearningplan

import java.util.UUID

fun aValidActionPlanSummary(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
): ActionPlanSummary =
  ActionPlanSummary(
    reference = reference,
    prisonNumber = prisonNumber,
  )
