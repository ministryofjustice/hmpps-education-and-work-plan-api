package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.timeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.timeline.aValidTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.assertThat
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType as TimelineEventTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventType as TimelineEventTypeEntity

class TimelineEventEntityMapperTest {

  private val mapper = TimelineEventEntityMapper()

  @Test
  fun `should map from domain to entity`() {
    // Given
    val reference = UUID.randomUUID()
    val sourceReference = UUID.randomUUID().toString()
    val prisonId = "BXI"
    val timelineEvent = aValidTimelineEvent(
      reference = reference,
      sourceReference = sourceReference,
      eventType = TimelineEventTypeDomain.STEP_UPDATED,
    )
    val expected = aValidTimelineEventEntity(
      reference = reference,
      sourceReference = sourceReference,
      eventType = TimelineEventTypeEntity.STEP_UPDATED,
      contextualInfo = emptyMap(),
      prisonId = prisonId,
      actionedBy = timelineEvent.actionedBy,
      timestamp = timelineEvent.timestamp,
      correlationId = timelineEvent.correlationId,
    )

    // When
    val actual = mapper.fromDomainToEntity(timelineEvent)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .usingRecursiveComparison()
      .ignoringFields("id", "createdAt")
      .isEqualTo(expected)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val reference = UUID.randomUUID()
    val sourceReference = UUID.randomUUID().toString()
    val prisonId = "BXI"
    val timelineEventEntity = aValidTimelineEventEntity(
      reference = reference,
      sourceReference = sourceReference,
      eventType = TimelineEventTypeEntity.STEP_UPDATED,
      contextualInfo = emptyMap(),
      prisonId = prisonId,
    )
    val expected = aValidTimelineEvent(
      reference = reference,
      sourceReference = sourceReference,
      eventType = TimelineEventTypeDomain.STEP_UPDATED,
      contextualInfo = emptyMap(),
      prisonId = prisonId,
      timestamp = timelineEventEntity.timestamp,
      correlationId = timelineEventEntity.correlationId,
    )

    // When
    val actual = mapper.fromEntityToDomain(timelineEventEntity)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }
}
