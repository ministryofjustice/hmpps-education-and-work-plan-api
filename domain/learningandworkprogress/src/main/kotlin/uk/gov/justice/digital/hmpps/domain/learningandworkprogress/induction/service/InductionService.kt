package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSummary
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
  private val inductionScheduleEventService: InductionScheduleEventService,
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
      return inductionSchedulePersistenceAdapter.createInductionSchedule(createInductionScheduleDto)
    }

  fun createOrUpdateInductionSchedule(prisonNumber: String, eventDate: Instant) {
    log.info { "Creating or updating induction schedule for prisoner [$prisonNumber]" }

    // Check if an induction schedule already exists.
    val existingSchedule = inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber)
    if (existingSchedule != null) {
      // Update existing schedule with the correct calculation rule and deadline date.
      val calculationRule = determineCalculationRule(prisonNumber)
      val updatedDeadlineDate = calculateDeadlineDate(eventDate)

      inductionSchedulePersistenceAdapter.updateSchedule(
        prisonNumber,
        calculationRule,
        updatedDeadlineDate,
      )
      return
    }

    // If no induction schedule exists, check for an existing induction.
    if (persistenceAdapter.getInduction(prisonNumber) != null) {
      log.info { "Induction already exists for prisoner [$prisonNumber], creating a review." }
      // TODO: Implement review creation
      return
    }

    // Create a new induction schedule.
    val newInductionSchedule = inductionSchedulePersistenceAdapter.createInductionSchedule(
      CreateInductionScheduleDto(
        prisonNumber,
        calculateDeadlineDate(eventDate),
        InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
      ),
    )
    inductionScheduleEventService.inductionScheduleCreated(newInductionSchedule)
  }

  private fun determineCalculationRule(prisonNumber: String): InductionScheduleCalculationRule {
    // TODO
    // Based on how long the prisoner has left to serve set the calculation rule
    // get the prisoners release date and use it to determine which of the following rules to apply:
    //    NEW_PRISON_ADMISSION
    //    EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE
    //    EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE
    //    EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE
    //    EXISTING_PRISONER_INDETERMINATE_SENTENCE
    //    EXISTING_PRISONER_ON_REMAND
    //    EXISTING_PRISONER_UN_SENTENCED

    // return a default one for now:
    return InductionScheduleCalculationRule.EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE
  }

  // This function will need to calculate the deadline date initially this will be the date the prisoner entered
  // prison plus an agreed number of days.
  private fun calculateDeadlineDate(eventDate: Instant): LocalDate {
    val europeLondon: ZoneId = ZoneId.of("Europe/London")
    val numberOfDaysToAdd = 20
    return eventDate.atZone(europeLondon).toLocalDate().plusDays(numberOfDaysToAdd.toLong())
  }
}
