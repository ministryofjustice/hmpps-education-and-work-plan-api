package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEvent
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.PRISON_ADMISSION
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.PRISON_RELEASE
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType.PRISON_TRANSFER
import uk.gov.justice.digital.hmpps.domain.timeline.aValidPrisonMovementTimelineEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidAdmissionPrisonMovementEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi.aValidPrisonMovementEvents
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.InstantMapper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class PrisonMovementEventsMapperTest {
  @InjectMocks
  private lateinit var prisonMovementEventsMapper: PrisonMovementEventsMapperImpl

  @Spy
  private lateinit var instantMapper: InstantMapper

  @Test
  fun `should map to TimelineEvents`() {
    // Given
    val prisonMovementEvents = aValidPrisonMovementEvents()
    val expected = listOf(
      aValidPrisonMovementTimelineEvent(
        sourceReference = "1",
        eventType = PRISON_ADMISSION,
        prisonId = "BMI",
        timestamp = LocalDate.now().minusMonths(6).toInstantAtStartOfDay(),
      ),
      aValidPrisonMovementTimelineEvent(
        sourceReference = "1",
        eventType = PRISON_RELEASE,
        prisonId = "BXI",
        timestamp = LocalDate.now().minusMonths(3).toInstantAtStartOfDay(),
      ),
      aValidPrisonMovementTimelineEvent(
        sourceReference = "1",
        eventType = PRISON_ADMISSION,
        prisonId = "BXI",
        timestamp = LocalDate.now().minusMonths(2).toInstantAtStartOfDay(),
      ),
      aValidPrisonMovementTimelineEvent(
        sourceReference = "1",
        eventType = PRISON_RELEASE,
        prisonId = "BXI",
        timestamp = LocalDate.now().minusMonths(1).toInstantAtStartOfDay(),
      ),
      aValidPrisonMovementTimelineEvent(
        sourceReference = "1",
        eventType = PRISON_TRANSFER,
        contextualInfo = "BMI",
        prisonId = "MDI",
        timestamp = LocalDate.now().minusMonths(5).toInstantAtStartOfDay(),
      ),
      aValidPrisonMovementTimelineEvent(
        sourceReference = "1",
        eventType = PRISON_TRANSFER,
        contextualInfo = "MDI",
        prisonId = "BXI",
        timestamp = LocalDate.now().minusMonths(4).toInstantAtStartOfDay(),
      ),

      aValidPrisonMovementTimelineEvent(
        sourceReference = "2",
        eventType = PRISON_ADMISSION,
        prisonId = "BXI",
        timestamp = LocalDate.now().minusMonths(2).toInstantAtStartOfDay(),
      ),
      aValidPrisonMovementTimelineEvent(
        sourceReference = "2",
        eventType = PRISON_RELEASE,
        prisonId = "BXI",
        timestamp = LocalDate.now().minusMonths(1).toInstantAtStartOfDay(),
      ),
    )

    // When
    val actual = prisonMovementEventsMapper.toTimelineEvents(prisonMovementEvents)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference", "correlationId").isEqualTo(expected)
  }

  @Test
  fun `should map to TimelineEvents given only one admission event`() {
    // Given
    val prisonMovementEvents = aValidPrisonMovementEvents(
      prisonBookings = mapOf(1L to listOf(aValidAdmissionPrisonMovementEvent())),
    )
    val expected = listOf(
      aValidPrisonMovementTimelineEvent(
        sourceReference = "1",
        eventType = PRISON_ADMISSION,
        prisonId = "BMI",
        timestamp = LocalDate.now().minusMonths(6).toInstantAtStartOfDay(),
      ),
    )

    // When
    val actual = prisonMovementEventsMapper.toTimelineEvents(prisonMovementEvents)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference", "correlationId").isEqualTo(expected)
  }

  @Test
  fun `should map to TimelineEvents given no prison movements`() {
    // Given
    val prisonMovementEvents = aValidPrisonMovementEvents(prisonBookings = emptyMap()) // should never happen
    val expected = emptyList<TimelineEvent>()

    // When
    val actual = prisonMovementEventsMapper.toTimelineEvents(prisonMovementEvents)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  private fun LocalDate.toInstantAtStartOfDay(): Instant = atStartOfDay(ZoneOffset.UTC).toInstant()
}
