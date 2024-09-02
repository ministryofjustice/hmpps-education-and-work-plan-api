package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service.EducationEventService

/**
 * Implementation of [EducationEventService] for performing additional asynchronous actions related to [PreviousQualifications] events.
 */
@Service
@Async
class AsyncEducationEventService : EducationEventService {
  override fun previousQualificationsCreated(createdInduction: PreviousQualifications) {
    // TODO - noop for now; implement behaviour once we understand what the requirements are (likely register timeline event)
  }
}
