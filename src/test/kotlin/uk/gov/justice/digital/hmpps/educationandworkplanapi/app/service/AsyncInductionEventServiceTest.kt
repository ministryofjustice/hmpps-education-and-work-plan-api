package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aFullyPopulatedInduction
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto

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

  @Mock
  private lateinit var userService: ManageUserService

  @Captor
  private lateinit var timelineEventCaptor: ArgumentCaptor<TimelineEvent>

  @Test
  fun `should handle induction created`() {
    // Given

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
    )

    val prisonNumber = aValidPrisonNumber()
    val induction = aFullyPopulatedInduction(prisonNumber = prisonNumber)
    val expectedTimelineEvent =
      with(induction) {
        TimelineEvent.newTimelineEvent(
          sourceReference = reference.toString(),
          eventType = TimelineEventType.INDUCTION_CREATED,
          prisonId = createdAtPrison,
          actionedBy = induction.createdBy!!,
          timestamp = induction.createdAt!!,
          contextualInfo = mapOf(
            TimelineEventContext.COMPLETED_INDUCTION_ENTERED_ONLINE_AT to induction.createdAt.toString(),
            TimelineEventContext.COMPLETED_INDUCTION_ENTERED_ONLINE_BY to "Alex Smith",
            TimelineEventContext.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_DATE to induction.completedDate.toString(),
            TimelineEventContext.COMPLETED_INDUCTION_NOTES to induction.note!!.content,
            TimelineEventContext.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY to "John Smith",
            TimelineEventContext.COMPLETED_INDUCTION_CONDUCTED_IN_PERSON_BY_ROLE to "Peer Mentor",
          ),
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
          timestamp = induction.lastUpdatedAt!!,
          contextualInfo = emptyMap(),
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
