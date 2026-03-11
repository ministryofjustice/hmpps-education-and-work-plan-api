package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.EducationAssessmentEvent
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.assessment.dto.CreateEducationAssessmentEventDto

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules for the EducationAssessmentEvent domain.
 */
class EducationAssessmentEventService(
  private val persistenceAdapter: EducationAssessmentEventPersistenceAdapter,
  private val eventService: EducationAssessmentEventEventService,
) {

  /**
   * Creates a new [EducationAssessmentEvent] from the specified [CreateEducationAssessmentEventDto].
   */
  fun createEducationAssessmentEvent(dto: CreateEducationAssessmentEventDto): EducationAssessmentEvent = with(dto) {
    log.info { "Creating Education Assessment Event for prisoner [$prisonNumber]" }

    return persistenceAdapter.createEducationAssessmentEvent(dto)
      .also {
        eventService.educationAssessmentEventCreated(it)
      }
  }

  /**
   * Returns all [EducationAssessmentEvent] records for the prisoner identified by their prison number.
   */
  fun getEducationAssessmentEvents(prisonNumber: String): List<EducationAssessmentEvent> = persistenceAdapter.getEducationAssessmentEvents(prisonNumber)
}
