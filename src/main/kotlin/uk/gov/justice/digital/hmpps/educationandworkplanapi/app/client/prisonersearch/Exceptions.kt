package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

class PrisonerSearchApiException(message: String, throwable: Throwable) : RuntimeException(message, throwable)

/**
 * Thrown when a specific prisoner is not returned by Prisoner Search API
 */
class PrisonerNotFoundException(prisonNumber: String) : RuntimeException("Prisoner [$prisonNumber] not returned by Prisoner Search API")

/**
 * Thrown when reception date is expected but missing, for the given prisoner
 */
class MissingReceptionDateException(prisonNumber: String) : RuntimeException("Reception date for Prisoner [$prisonNumber] is missing.")

/**
 * Thrown when sentence start date is expected but missing, for the given prisoner
 */
class MissingSentenceStartDateException(prisonNumber: String) : RuntimeException("Sentence start date for Prisoner [$prisonNumber] is missing")
