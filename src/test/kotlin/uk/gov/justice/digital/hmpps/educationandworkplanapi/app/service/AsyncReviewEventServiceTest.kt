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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewConductedBy
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.aValidCompletedReview
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent.Companion.newTimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_ENTERED_ONLINE_AT
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventContext.COMPLETED_REVIEW_ENTERED_ONLINE_BY
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.manageusers.UserDetailsDto

@ExtendWith(MockitoExtension::class)
class AsyncReviewEventServiceTest {
  @InjectMocks
  private lateinit var reviewEventService: AsyncReviewEventService

  @Mock
  private lateinit var timelineService: TimelineService

  @Mock
  private lateinit var telemetryService: TelemetryService

  @Mock
  private lateinit var userService: ManageUserService

  @Captor
  private lateinit var timelineEventCaptor: ArgumentCaptor<TimelineEvent>

  companion object {
    private val IGNORED_FIELDS = arrayOf("reference", "correlationId")
  }

  @Test
  fun `should handle completed review event given review was not conducted by someone else`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val completedReview = aValidCompletedReview(
      prisonNumber = prisonNumber,
      createdBy = "asmith_gen",
      createdAtPrison = "BXI",
      conductedBy = null,
    )

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
    )

    val expectedTimelineEvent = newTimelineEvent(
      sourceReference = completedReview.reference.toString(),
      eventType = TimelineEventType.ACTION_PLAN_REVIEW_COMPLETED,
      prisonId = completedReview.createdAtPrison,
      actionedBy = completedReview.createdBy,
      actionedByDisplayName = null,
      timestamp = completedReview.createdAt,
      contextualInfo = mapOf(
        COMPLETED_REVIEW_ENTERED_ONLINE_AT to completedReview.createdAt.toString(),
        COMPLETED_REVIEW_ENTERED_ONLINE_BY to "Alex Smith",
        COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE to completedReview.completedDate.toString(),
      ),
    )

    // When
    reviewEventService.reviewCompleted(completedReview)

    // Then
    verify(timelineService).recordTimelineEvent(eq(prisonNumber), capture(timelineEventCaptor))
    verify(telemetryService).trackReviewCompleted(completedReview)
    verify(userService).getUserDetails("asmith_gen")

    assertThat(timelineEventCaptor.value).usingRecursiveComparison().ignoringFields(*IGNORED_FIELDS).isEqualTo(expectedTimelineEvent)
  }

  @Test
  fun `should handle completed review event given review was conducted by someone else`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val completedReview = aValidCompletedReview(
      prisonNumber = prisonNumber,
      createdBy = "asmith_gen",
      createdAtPrison = "BXI",
      conductedBy = ReviewConductedBy("Fred Bloggs", "Peer Mentor"),
    )

    given(userService.getUserDetails(any())).willReturn(
      UserDetailsDto("asmith_gen", true, "Alex Smith"),
    )

    val expectedTimelineEvent = newTimelineEvent(
      sourceReference = completedReview.reference.toString(),
      eventType = TimelineEventType.ACTION_PLAN_REVIEW_COMPLETED,
      prisonId = completedReview.createdAtPrison,
      actionedBy = completedReview.createdBy,
      actionedByDisplayName = null,
      timestamp = completedReview.createdAt,
      contextualInfo = mapOf(
        COMPLETED_REVIEW_ENTERED_ONLINE_AT to completedReview.createdAt.toString(),
        COMPLETED_REVIEW_ENTERED_ONLINE_BY to "Alex Smith",
        COMPLETED_REVIEW_CONDUCTED_IN_PERSON_DATE to completedReview.completedDate.toString(),
        COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY to "Fred Bloggs",
        COMPLETED_REVIEW_CONDUCTED_IN_PERSON_BY_ROLE to "Peer Mentor",
      ),
    )

    // When
    reviewEventService.reviewCompleted(completedReview)

    // Then
    verify(timelineService).recordTimelineEvent(eq(prisonNumber), capture(timelineEventCaptor))
    verify(telemetryService).trackReviewCompleted(completedReview)
    verify(userService).getUserDetails("asmith_gen")

    assertThat(timelineEventCaptor.value).usingRecursiveComparison().ignoringFields(*IGNORED_FIELDS).isEqualTo(expectedTimelineEvent)
  }
}
