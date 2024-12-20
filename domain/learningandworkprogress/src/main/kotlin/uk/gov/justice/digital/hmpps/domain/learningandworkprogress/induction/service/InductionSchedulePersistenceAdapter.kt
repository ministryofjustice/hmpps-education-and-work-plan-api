package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleHistory
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateInductionScheduleStatusDto
import java.time.LocalDate

/**
 * Persistence Adapter for [InductionSchedule] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo,
 * Redis etc.
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by.
 * [InductionService].
 */
interface InductionSchedulePersistenceAdapter {
  /**
   * Persists a new [InductionSchedule] for a prisoner.
   *
   * @return The [InductionSchedule] with any newly generated values (if applicable).
   */
  fun createInductionSchedule(createInductionScheduleDto: CreateInductionScheduleDto): InductionSchedule

  /**
   * Retrieves an [InductionSchedule] for a given Prisoner. Returns `null` if the [InductionSchedule] does not exist.
   */
  fun getInductionSchedule(prisonNumber: String): InductionSchedule?

  fun updateSchedule(
    prisonNumber: String,
    calculationRule: InductionScheduleCalculationRule,
    deadlineDate: LocalDate,
  ): InductionSchedule

  /**
   * Retrieves the history of a Prisoner's [InductionSchedule]s.
   */
  fun getInductionScheduleHistory(prisonNumber: String): List<InductionScheduleHistory>

  /**
   * Update the Induction schedule status and the deadlineDate.
   */
  fun updateInductionScheduleStatus(updateInductionScheduleStatusDto: UpdateInductionScheduleStatusDto): InductionSchedule
}
