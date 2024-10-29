package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

/**
 * Interface defining a series of Induction Schedule lifecycle event methods.
 */
interface InductionScheduleEventService {

  /**
   * Implementations providing custom code for when a [InductionSchedule] is created.
   */
  fun inductionScheduleCreated(createdInductionSchedule: InductionSchedule)

  /**
   * Implementations providing custom code for when a [InductionSchedule] is updated.
   */
  fun inductionScheduleUpdated(updatedInductionSchedule: InductionSchedule)
}
