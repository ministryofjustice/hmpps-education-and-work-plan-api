package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

/**
 * Thrown when a Induction does not exist for a given Prisoner.
 */
class InductionNotFoundException(val prisonNumber: String) :
  RuntimeException("Induction not found for prisoner [$prisonNumber]")
