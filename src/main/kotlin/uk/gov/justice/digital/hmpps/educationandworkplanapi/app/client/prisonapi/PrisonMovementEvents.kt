package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

/**
 * Holds a list Prisoner's movements in or out of Prison, or transfers between Prisons, for each "Booking". The
 * latter can be described as the time spent in Prison for one or more related Sentences. If the Prisoner is released
 * into the community and then subsequently prosecuted for a separate offence later, then this will result in a new
 * "Booking".
 *
 * Note that this is a high level abstraction from the prison-api and is intended to capture when a Prisoner was in
 * each Prison, rather than temporary absences (TAPs) for things like hospital appointments etc.
 */
data class PrisonMovementEvents(
  val prisonNumber: String,
  val prisonMovements: Map<Long, List<PrisonMovementEvent>>,
)
