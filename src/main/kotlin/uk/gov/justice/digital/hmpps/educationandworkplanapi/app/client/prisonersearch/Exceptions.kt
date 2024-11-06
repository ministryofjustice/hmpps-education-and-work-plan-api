package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch

class PrisonerSearchApiException(message: String, throwable: Throwable) : RuntimeException(message, throwable)

/**
 * Thrown when a specific prisoner is not returned by Prisoner Search API
 */
class PrisonerNotFoundException(prisonNumber: String) :
  RuntimeException("Prisoner [$prisonNumber] not returned by Prisoner Search API")
