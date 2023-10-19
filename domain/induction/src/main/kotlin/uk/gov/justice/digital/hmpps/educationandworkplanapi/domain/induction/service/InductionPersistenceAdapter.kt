package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto

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
   * Persists a new [Induction] that has taken place for a prisoner.
   *
   * @return The [Induction] with any newly generated values (if applicable).
   */
  fun createInduction(induction: CreateInductionDto): Induction

  /**
   * Retrieves an [Induction] for a given Prisoner. Returns `null` if the [Induction] does not exist.
   */
  fun getInduction(prisonNumber: String): Induction?
}
