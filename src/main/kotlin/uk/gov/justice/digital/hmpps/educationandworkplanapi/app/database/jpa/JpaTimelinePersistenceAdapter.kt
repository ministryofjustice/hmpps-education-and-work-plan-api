package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.domain.timeline.Timeline
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelinePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEntity.Companion.newTimelineForPrisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline.TimelineEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline.TimelineEventEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.TimelineRepository

private val log = KotlinLogging.logger {}

@Component
class JpaTimelinePersistenceAdapter(
  private val timelineRepository: TimelineRepository,
  private val timelineMapper: TimelineEntityMapper,
  private val timelineEventMapper: TimelineEventEntityMapper,
) : TimelinePersistenceAdapter {

  @Transactional
  override fun recordTimelineEvent(prisonNumber: String, event: TimelineEvent): TimelineEvent {
    log.info { "Recording TimelineEvent [${event.eventType}] for prisoner [$prisonNumber]" }

    val timelineEntity = findOrCreateTimelineEntity(prisonNumber)
    val timelineEventEntity = timelineEventMapper.fromDomainToEntity(event)

    with(timelineEntity) {
      addEvent(timelineEventEntity)
      timelineRepository.saveAndFlush(this)
    }

    // use the persisted entity with the populated JPA fields, rather than the non persisted entity reference above
    val persisted = timelineEntity.events.first { it.reference == timelineEventEntity.reference }
    return timelineEventMapper.fromEntityToDomain(persisted)
  }

  @Transactional
  override fun recordTimelineEvents(prisonNumber: String, events: List<TimelineEvent>): Timeline {
    log.info { "Recording [${events.size}] TimelineEvents for prisoner [$prisonNumber]" }

    val timelineEntity = findOrCreateTimelineEntity(prisonNumber)
    events.forEach {
      timelineEntity.addEvent(timelineEventMapper.fromDomainToEntity(it))
    }

    timelineRepository.saveAndFlush(timelineEntity)
    return timelineMapper.fromEntityToDomain(timelineEntity)
  }

  @Transactional
  override fun getTimelineForPrisoner(prisonNumber: String): Timeline? {
    log.info { "Getting Timeline for prisoner [$prisonNumber]" }

    val timelineEntity = timelineRepository.findByPrisonNumber(prisonNumber)
    return if (timelineEntity != null) timelineMapper.fromEntityToDomain(timelineEntity) else null
  }

  private fun findOrCreateTimelineEntity(prisonNumber: String): TimelineEntity {
    var timelineEntity = timelineRepository.findByPrisonNumber(prisonNumber)
    if (timelineEntity == null) {
      log.info { "Creating new Timeline for prisoner [$prisonNumber]" }
      timelineEntity = newTimelineForPrisoner(prisonNumber)
    }
    return timelineEntity
  }
}
