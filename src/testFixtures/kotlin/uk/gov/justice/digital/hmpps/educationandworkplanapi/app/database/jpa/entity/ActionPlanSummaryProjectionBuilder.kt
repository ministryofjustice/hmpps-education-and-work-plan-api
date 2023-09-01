package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import java.time.LocalDate
import java.util.UUID

fun aValidActionPlanSummaryProjection(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234BC",
  reviewDate: LocalDate? = LocalDate.now().plusMonths(6),
): ActionPlanSummaryProjection =
  ActionPlanSummaryProjection(
    reference = reference,
    prisonNumber = prisonNumber,
    reviewDate = reviewDate,
  )
