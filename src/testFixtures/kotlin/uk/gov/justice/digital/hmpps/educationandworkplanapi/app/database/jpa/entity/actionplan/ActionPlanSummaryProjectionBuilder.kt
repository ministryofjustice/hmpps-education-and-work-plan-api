package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

import java.util.UUID

fun aValidActionPlanSummaryProjection(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
): ActionPlanSummaryProjection =
  ActionPlanSummaryProjection(
    reference = reference,
    prisonNumber = prisonNumber,
  )
