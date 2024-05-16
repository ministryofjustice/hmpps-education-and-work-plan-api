package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.Induction

/**
 * Interface defining a series of Induction lifecycle event methods.
 */
interface InductionEventService {

  /**
   * Implementations providing custom code for when a [Induction] is created.
   */
  fun inductionCreated(createdInduction: Induction)

  /**
   * Implementations providing custom code for when a [Induction] is updated.
   */
  fun inductionUpdated(updatedInduction: Induction)
}
