package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

/**
 * Thrown when a Goal cannot be created, for example because it is missing mandatory data.
 */
class InvalidGoalException(message: String) : RuntimeException(message)
