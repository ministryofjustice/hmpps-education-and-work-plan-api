package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.UpdatedInductionScheduleStatus

/**
 * Interface defining a series of Induction schedule lifecycle event methods.
 */
interface InductionScheduleEventService {

  /**
   * Implementations providing custom code for when a Induction schedule is updated.
   */
  fun inductionScheduleStatusUpdated(updatedInductionScheduleStatus: UpdatedInductionScheduleStatus)
}
