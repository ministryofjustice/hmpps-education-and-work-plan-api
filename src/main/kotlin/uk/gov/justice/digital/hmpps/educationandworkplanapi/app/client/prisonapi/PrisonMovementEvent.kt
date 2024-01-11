package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import java.time.LocalDate

/**
 * Represents a Prisoner's movement in or out of prison, or a transfer between Prisons. In the case of the latter, both
 * `fromPrisonId` and `toPrisonId` should be populated, otherwise only one of them will be.
 *
 * Note that this is a high level abstraction from the prison-api and is intended to capture when a Prisoner was in
 * each Prison, rather than temporary absences (TAPs) for things like hospital appointments etc.
 */
data class PrisonMovementEvent(
  val date: LocalDate,
  val movementType: PrisonMovementType,
  val fromPrisonId: String?,
  val toPrisonId: String?,
)
