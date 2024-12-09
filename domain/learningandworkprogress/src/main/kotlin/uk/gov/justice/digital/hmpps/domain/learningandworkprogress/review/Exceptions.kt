package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

/**
 * Thrown when a Review Schedule does not exist for a given Prisoner.
 */
class ReviewScheduleNotFoundException(val prisonNumber: String) :
  RuntimeException("Review Schedule not found for prisoner [$prisonNumber]")

/**
 * Thrown when a prisoner already has an active Review Schedule. A Prisoner cannot have more than 1 active Review Schedules.
 */
class ActiveReviewScheduleAlreadyExistsException(val prisonNumber: String) :
  RuntimeException("Prisoner [$prisonNumber] already has an active Review Schedule.")

class InvalidReviewScheduleStatusException(val prisonNumber: String, val fromStatus: String, val toStatus: String) :
  RuntimeException("Invalid Review Schedule status transition for prisoner [$prisonNumber] status from $fromStatus to $toStatus")
