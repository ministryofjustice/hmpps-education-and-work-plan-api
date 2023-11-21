package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline.aValidTimelineEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.TimelineRepository

@ExtendWith(MockitoExtension::class)
class PrisonerInductionTimelineLookupServiceTest {

  @Mock
  private lateinit var timelineRepository: TimelineRepository

  @InjectMocks
  private lateinit var timelineLookupService: PrisonerInductionTimelineLookupService

  @Test
  fun `should find prisoners who have had inductions`() {
    // Given
    val prisonNumber1 = aValidPrisonNumber()
    val prisonNumber2 = "B5678CD"
    val prisonNumber3 = "C1111EF"
    given(timelineRepository.findAll()).willReturn(
      listOf(
        aValidTimelineEntity(
          prisonNumber = prisonNumber1,
          events = mutableListOf(
            aValidTimelineEventEntity(eventType = TimelineEventType.INDUCTION_CREATED),
            aValidTimelineEventEntity(eventType = TimelineEventType.INDUCTION_UPDATED),
            aValidTimelineEventEntity(eventType = TimelineEventType.GOAL_CREATED),
          ),
        ),
        aValidTimelineEntity(
          prisonNumber = prisonNumber2,
          events = mutableListOf(
            aValidTimelineEventEntity(eventType = TimelineEventType.INDUCTION_CREATED),
            aValidTimelineEventEntity(eventType = TimelineEventType.INDUCTION_UPDATED),
            aValidTimelineEventEntity(eventType = TimelineEventType.GOAL_CREATED),
          ),
        ),
        aValidTimelineEntity(
          prisonNumber = prisonNumber3,
          events = mutableListOf(
            // for testing only - should never exist without an induction
            aValidTimelineEventEntity(eventType = TimelineEventType.GOAL_CREATED),
          ),
        ),
      ),
    )
    val expectedPrisonNumbers = listOf(prisonNumber1, prisonNumber2)

    // When
    val prisonNumbers = timelineLookupService.getPrisonNumbersToMigrate()

    // Then
    assertThat(prisonNumbers).isEqualTo(expectedPrisonNumbers)
  }
}
