package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

/**
 * Thrown when a Review Schedule does not exist for a given Prisoner.
 */
class ReviewScheduleNotFoundException(val prisonNumber: String) :
  RuntimeException("Review Schedule not found for prisoner [$prisonNumber]")
