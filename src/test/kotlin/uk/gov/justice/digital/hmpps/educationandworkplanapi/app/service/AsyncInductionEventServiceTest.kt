package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService

@ExtendWith(MockitoExtension::class)
class AsyncInductionEventServiceTest {

  companion object {
    private val IGNORED_FIELDS = arrayOf("reference", "correlationId")
  }

  @InjectMocks
  private lateinit var inductionEventService: AsyncInductionEventService

  @Mock
  private lateinit var timelineService: TimelineService

  @Mock
  private lateinit var telemetryService: TelemetryService

  @Captor
  private lateinit var timelineEventCaptor: ArgumentCaptor<TimelineEvent>

  @Test
  fun `should handle induction created`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val induction = aFullyPopulatedInduction(prisonNumber = prisonNumber)
    val expectedTimelineEvent =
      with(induction) {
        TimelineEvent.newTimelineEvent(
          sourceReference = reference.toString(),
          eventType = TimelineEventType.INDUCTION_CREATED,
          prisonId = createdAtPrison,
          actionedBy = induction.createdBy!!,
          actionedByDisplayName = induction.createdByDisplayName,
          timestamp = induction.createdAt!!,
        )
      }

    // When
    inductionEventService.inductionCreated(induction)

    // Then
    verify(timelineService).recordTimelineEvent(eq(prisonNumber), capture(timelineEventCaptor))
    verify(telemetryService).trackInductionCreated(induction)
    assertThat(timelineEventCaptor.value).usingRecursiveComparison().ignoringFields(*IGNORED_FIELDS).isEqualTo(expectedTimelineEvent)
  }

  @Test
  fun `should handle induction updated`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val induction = aFullyPopulatedInduction(prisonNumber = prisonNumber)
    val expectedTimelineEvent =
      with(induction) {
        TimelineEvent.newTimelineEvent(
          sourceReference = reference.toString(),
          eventType = TimelineEventType.INDUCTION_UPDATED,
          prisonId = lastUpdatedAtPrison,
          actionedBy = induction.lastUpdatedBy!!,
          actionedByDisplayName = induction.lastUpdatedByDisplayName,
          timestamp = induction.lastUpdatedAt!!,
        )
      }

    // When
    inductionEventService.inductionUpdated(induction)

    // Then
    verify(timelineService).recordTimelineEvent(eq(prisonNumber), capture(timelineEventCaptor))
    verify(telemetryService).trackInductionUpdated(induction)
    assertThat(timelineEventCaptor.value).usingRecursiveComparison().ignoringFields(*IGNORED_FIELDS).isEqualTo(expectedTimelineEvent)
  }
}
