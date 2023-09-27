package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.service.events.aValidCiagInductionCreatedEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.service.events.aValidCiagInductionUpdatedEvent

internal class CiagInductionTimelineEventTest : IntegrationTestBase() {

  @Test
  fun `should process CIAG induction created event`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionCreatedEvent = aValidCiagInductionCreatedEvent(prisonNumber = prisonNumber)

    // When
    snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopicArn)
        .message(objectMapper.writeValueAsString(inductionCreatedEvent))
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String")
              .stringValue(EventType.CIAG_INDUCTION_CREATED.eventType).build(),
          ),
        )
        .build(),
    )

    // Then
    await.untilAsserted {
      verify(inboundEventsServiceSpy).process(inductionCreatedEvent)
    }
    val timeline = getTimeline(prisonNumber)
    assertThat(timeline).hasNumberOfEvents(1)
    assertThat(timeline.events[0])
      .hasEventType(TimelineEventType.INDUCTION_CREATED)
      .hasPrisonId(inductionCreatedEvent.prisonId())
      .hasSourceReference(inductionCreatedEvent.reference())
      .wasActionedBy(inductionCreatedEvent.userId())
  }

  @Test
  fun `should process CIAG induction updated event`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val inductionUpdatedEvent = aValidCiagInductionUpdatedEvent(prisonNumber = prisonNumber)

    // When
    snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopicArn)
        .message(objectMapper.writeValueAsString(inductionUpdatedEvent))
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String")
              .stringValue(EventType.CIAG_INDUCTION_UPDATED.eventType).build(),
          ),
        )
        .build(),
    )

    // Then
    await.untilAsserted {
      verify(inboundEventsServiceSpy).process(inductionUpdatedEvent)
    }
    val timeline = getTimeline(prisonNumber)
    assertThat(timeline).hasNumberOfEvents(1)
    assertThat(timeline.events[0])
      .hasEventType(TimelineEventType.INDUCTION_UPDATED)
      .hasPrisonId(inductionUpdatedEvent.prisonId())
      .hasSourceReference(inductionUpdatedEvent.reference())
      .wasActionedBy(inductionUpdatedEvent.userId())
  }
}
