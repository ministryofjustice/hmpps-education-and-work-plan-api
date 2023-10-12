package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction

/**
 * Service class exposing methods that implement the business rules for the Induction domain.
 *
 * Applications using Inductions must new up an instance of this class providing an implementation of
 * [InductionPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class InductionService(
  private val persistenceAdapter: InductionPersistenceAdapter,
) {

  /**
   * Records an [Induction] that has taken place for a prisoner.
   */
  fun createInduction(prisonNumber: String, induction: Induction) =
    persistenceAdapter.saveInduction(prisonNumber, induction)
}
