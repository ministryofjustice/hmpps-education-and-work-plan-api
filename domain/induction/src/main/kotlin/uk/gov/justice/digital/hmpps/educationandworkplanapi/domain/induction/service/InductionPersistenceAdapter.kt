package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction

/**
 * Persistence Adapter for [Induction] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo,
 * Redis etc.
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by.
 * [InductionService].
 */
interface InductionPersistenceAdapter {

  /**
   * Records an [Induction] that has taken place for a prisoner.
   *
   * @return The [Induction] with any newly generated values (if applicable).
   */
  fun saveInduction(prisonNumber: String, induction: Induction): Induction
}
