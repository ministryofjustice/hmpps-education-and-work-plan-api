package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal

@ExtendWith(MockitoExtension::class)
class GoalServiceTest {
  @InjectMocks
  private lateinit var service: GoalService

  @Mock
  private lateinit var persistenceAdapter: GoalPersistenceAdapter

  @Test
  fun `should create goal`() {
    // Given
    val goal = aValidGoal()
    given(persistenceAdapter.saveGoal(any())).willReturn(goal)

    // When
    service.saveGoal(goal)

    // Then
    verify(persistenceAdapter).saveGoal(goal)
  }
}
