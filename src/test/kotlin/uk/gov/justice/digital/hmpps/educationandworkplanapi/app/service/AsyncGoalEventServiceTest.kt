package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal

@ExtendWith(MockitoExtension::class)
class AsyncGoalEventServiceTest {

  @InjectMocks
  private lateinit var goalEventService: AsyncGoalEventService

  @Mock
  private lateinit var telemetryService: TelemetryService

  @Test
  fun `should send app insights telemetry event given goal created`() {
    // Given
    val createdGoal = aValidGoal()

    // When
    goalEventService.goalCreated(createdGoal)

    // Then
    await.untilAsserted {
      verify(telemetryService).trackGoalCreateEvent(createdGoal)
    }
  }

  @Test
  fun `should send app insights telemetry event given goal updated`() {
    // Given
    val updatedGoal = aValidGoal()

    // When
    goalEventService.goalUpdated(updatedGoal)

    // Then
    await.untilAsserted {
      verify(telemetryService).trackGoalUpdateEvent(updatedGoal)
    }
  }
}
