package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InductionSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.UpdateInductionDto

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
  fun createInduction(createInductionDto: CreateInductionDto): Induction

  /**
   * Retrieves an [Induction] for a given Prisoner. Returns `null` if the [Induction] does not exist.
   */
  fun getInduction(prisonNumber: String): Induction?

  /**
   * Updates an [Induction] identified by its `prisonNumber`.
   */
  fun updateInduction(updateInductionDto: UpdateInductionDto): Induction?

  /**
   * Returns a [List] of [InductionSummary]s for each matching prisoner (in the provided [List] of prison numbers)
   * that has an Induction. The list can be empty, but not null.
   */
  fun getInductionSummaries(prisonNumbers: List<String>): List<InductionSummary>
}
