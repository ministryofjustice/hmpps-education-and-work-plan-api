package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications

/**
 * Persistence Adapter for Education related classes.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo,
 * Redis etc.
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by.
 * [EducationService].
 */
interface EducationPersistenceAdapter {
  /**
   * Retrieves a [PreviousQualifications] for a given Prisoner. Returns `null` if the [PreviousQualifications] instance does not exist.
   */
  fun getPreviousQualifications(prisonNumber: String): PreviousQualifications?
}
