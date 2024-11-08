package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionDto

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
  private val inductionEventService: InductionEventService,
  private val inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
) {

  /**
   * Records an [Induction] that has taken place for a prisoner.
   */
  fun createInduction(createInductionDto: CreateInductionDto): Induction =
    with(createInductionDto) {
      log.info { "Creating Induction for prisoner [$prisonNumber]" }

      if (persistenceAdapter.getInduction(prisonNumber) != null) {
        throw InductionAlreadyExistsException(prisonNumber)
      }

      return persistenceAdapter.createInduction(createInductionDto)
        .also {
          inductionEventService.inductionCreated(it)
        }
    }

  /**
   * Returns the [Induction] for the prisoner identified by their prison number. Otherwise, throws
   * [InductionNotFoundException] if it cannot be found.
   */
  fun getInductionForPrisoner(prisonNumber: String): Induction =
    persistenceAdapter.getInduction(prisonNumber) ?: throw InductionNotFoundException(prisonNumber)

  /**
   * Updates an [Induction], identified by its `prisonNumber`, from the specified [UpdateInductionDto].
   * Throws [InductionNotFoundException] if the [Induction] to be updated cannot be found.
   */
  fun updateInduction(updateInductionDto: UpdateInductionDto): Induction {
    val prisonNumber = updateInductionDto.prisonNumber
    log.info { "Updating Induction for prisoner [$prisonNumber]" }

    return persistenceAdapter.updateInduction(updateInductionDto)
      ?.also {
        inductionEventService.inductionUpdated(it)
      }
      ?: throw InductionNotFoundException(prisonNumber).also {
        log.info { "Induction for prisoner [$prisonNumber] not found" }
      }
  }

  fun getInductionSummaries(prisonNumbers: List<String>): List<InductionSummary> {
    log.debug { "Retrieving Induction Summaries for ${prisonNumbers.size} prisoners" }
    return if (prisonNumbers.isNotEmpty()) persistenceAdapter.getInductionSummaries(prisonNumbers) else emptyList()
  }

  fun createInductionSchedule(createInductionScheduleDto: CreateInductionScheduleDto): InductionSchedule =
    with(createInductionScheduleDto) {
      log.info { "Creating Induction Schedule for prisoner [$prisonNumber]" }

      if (inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber) != null) {
        throw InductionScheduleAlreadyExistsException(prisonNumber)
      }
      if (persistenceAdapter.getInduction(prisonNumber) != null) {
        throw InductionAlreadyExistsException(prisonNumber)
      }

      inductionSchedulePersistenceAdapter.createInductionSchedule(createInductionScheduleDto)
    }

  fun getInductionScheduleForPrisoner(prisonNumber: String): InductionSchedule =
    inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber)
      ?: throw InductionScheduleNotFoundException(prisonNumber)
}
