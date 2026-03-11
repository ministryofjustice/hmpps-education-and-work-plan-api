package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.EducationAssessmentEvent
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.dto.CreateEducationAssessmentEventDto

/**
 * Persistence Adapter for [EducationAssessmentEvent] records.
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by
 * [EducationAssessmentEventService].
 */
interface EducationAssessmentEventPersistenceAdapter {

  /**
   * Persists a new [EducationAssessmentEvent].
   *
   * @return The [EducationAssessmentEvent] with any newly generated values (if applicable).
   */
  fun createEducationAssessmentEvent(dto: CreateEducationAssessmentEventDto): EducationAssessmentEvent

  /**
   * Retrieves all [EducationAssessmentEvent] records for a given prisoner. Returns an empty list if none exist.
   */
  fun getEducationAssessmentEvents(prisonNumber: String): List<EducationAssessmentEvent>
}
