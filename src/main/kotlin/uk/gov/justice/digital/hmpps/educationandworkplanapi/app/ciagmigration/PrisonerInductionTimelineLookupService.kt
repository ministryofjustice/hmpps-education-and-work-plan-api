package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.TimelineRepository

@Service
class PrisonerInductionTimelineLookupService(private val timelineRepository: TimelineRepository) {

  @Transactional(readOnly = true)
  fun getPrisonNumbersToMigrate(): List<String> {
    val timelines = timelineRepository.findAll()

    return timelines
      .filter { includesInductionCreatedEvent(it) }
      .map { it.prisonNumber }
  }

  private fun includesInductionCreatedEvent(timeline: TimelineEntity) =
    timeline.events?.any { it.eventType == TimelineEventType.INDUCTION_CREATED } ?: false
}
