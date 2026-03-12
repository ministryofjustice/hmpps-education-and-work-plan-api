package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.EducationAssessmentEvent

/**
 * Interface defining lifecycle event methods for [EducationAssessmentEvent].
 */
interface EducationAssessmentEventEventService {

  /**
   * Implementations providing custom code for when an [EducationAssessmentEvent] is created.
   */
  fun educationAssessmentEventCreated(educationAssessmentEvent: EducationAssessmentEvent)
}
