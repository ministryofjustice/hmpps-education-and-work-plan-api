package uk.gov.justice.digital.hmpps.domain.induction

/**
 * Thrown when am Induction does not exist for a given Prisoner.
 */
class InductionNotFoundException(val prisonNumber: String) :
  RuntimeException("Induction not found for prisoner [$prisonNumber]")

/**
 * Thrown when an Attempt is made to create an Induction for a prisoner who already has one.
 */
class InductionAlreadyExistsException(message: String) : RuntimeException(message)
