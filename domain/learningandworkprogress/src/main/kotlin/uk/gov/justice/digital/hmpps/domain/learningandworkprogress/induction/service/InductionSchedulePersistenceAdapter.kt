package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto

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
}
