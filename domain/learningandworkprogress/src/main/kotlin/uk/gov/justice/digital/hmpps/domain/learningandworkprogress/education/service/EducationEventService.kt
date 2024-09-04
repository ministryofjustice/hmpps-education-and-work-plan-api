package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications

/**
 * Interface defining a series of PreviousQualifications lifecycle event methods.
 */
interface EducationEventService {

  /**
   * Implementations providing custom code for when a prisoner's record of [PreviousQualifications] is created.
   */
  fun previousQualificationsCreated(createdInduction: PreviousQualifications)
}
