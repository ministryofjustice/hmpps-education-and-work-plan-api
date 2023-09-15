package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline

/**
 * Thrown when a Timeline does not exist for a given Prisoner.
 */
class TimelineNotFoundException(val prisonNumber: String) :
  RuntimeException("Timeline not found for prisoner [$prisonNumber]")
