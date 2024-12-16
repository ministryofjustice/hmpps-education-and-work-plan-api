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

class InvalidReviewScheduleStatusException(val prisonNumber: String, fromStatus: ReviewScheduleStatus, toStatus: ReviewScheduleStatus) :
  RuntimeException("Invalid Review Schedule status transition for prisoner [$prisonNumber] status from $fromStatus to $toStatus")

sealed class InvalidReviewScheduleException(val prisonNumber: String, val sentenceType: SentenceType, message: String) : RuntimeException(message) {

  class ReviewScheduleInvalidSentenceTypeException(prisonNumber: String, sentenceType: SentenceType) :
    InvalidReviewScheduleException(prisonNumber, sentenceType, "Cannot create Review Schedule for prisoner [$prisonNumber]. Sentence type $sentenceType is not supported")

  class ReviewScheduleNoReleaseDateForSentenceTypeException(prisonNumber: String, sentenceType: SentenceType) :
    InvalidReviewScheduleException(prisonNumber, sentenceType, "Cannot create Review Schedule for prisoner [$prisonNumber]. Sentence type $sentenceType with no release date is not supported")
}
