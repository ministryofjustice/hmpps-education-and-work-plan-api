package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan

import java.util.UUID

data class ActionPlanSummaryProjection(
  val reference: UUID,
  val prisonNumber: String,
)
