package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education

/**
 * Thrown when an Education does not exist for a given Prisoner.
 */
class EducationNotFoundException(val prisonNumber: String) :
  RuntimeException("Education not found for prisoner [$prisonNumber]")

/**
 * Thrown when an attempt is made to create an Education for a prisoner who already has one.
 */
class EducationAlreadyExistsException(val prisonNumber: String) :
  RuntimeException("An Education already exists for prisoner $prisonNumber")
