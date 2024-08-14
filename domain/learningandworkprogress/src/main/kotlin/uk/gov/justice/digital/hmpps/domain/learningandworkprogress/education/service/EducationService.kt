package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.EducationNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules for the Education domain.
 *
 * Applications using Education must new up an instance of this class providing an implementation of
 * [EducationPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class EducationService(private val persistenceAdapter: EducationPersistenceAdapter) {

  /**
   * Returns the [PreviousQualifications] for the prisoner identified by their prison number. Otherwise, throws
   * [EducationNotFoundException] if it cannot be found.
   */
  fun getPreviousQualificationsForPrisoner(prisonNumber: String): PreviousQualifications =
    persistenceAdapter.getPreviousQualifications(prisonNumber) ?: throw EducationNotFoundException(prisonNumber)
}
