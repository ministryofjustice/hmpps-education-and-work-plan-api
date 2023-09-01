package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import java.time.LocalDate
import java.util.UUID

/**
 * A subset of a Prisoner's 'Action Plan', which excludes the list of Goals (mainly for performance reasons so that a
 * large number of these can be provided - e.g. in an HTTP response).
 */
data class ActionPlanSummary(
  val reference: UUID,
  val prisonNumber: String,
  val reviewDate: LocalDate?,
)
