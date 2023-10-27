package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.Induction
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InductionAlreadyExistsException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.CreateInductionDto

private val log = KotlinLogging.logger {}

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
  fun createInduction(createInductionDto: CreateInductionDto): Induction =
    with(createInductionDto) {
      log.info { "Creating Induction for prisoner [$prisonNumber]" }

      if (persistenceAdapter.getInduction(prisonNumber) != null) {
        throw InductionAlreadyExistsException("An Induction already exists for prisoner $prisonNumber")
      }

      return persistenceAdapter.createInduction(createInductionDto)
    }

  /**
   * Returns the [Induction] for the prisoner identified by their prison number. Otherwise, throws
   * [InductionNotFoundException] if it cannot be found.
   */
  fun getInductionForPrisoner(prisonNumber: String): Induction =
    persistenceAdapter.getInduction(prisonNumber) ?: throw InductionNotFoundException(prisonNumber)
}
