package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidUpdateGoalDto

@ExtendWith(MockitoExtension::class)
class GoalServiceTest {
  @InjectMocks
  private lateinit var service: GoalService

  @Mock
  private lateinit var persistenceAdapter: GoalPersistenceAdapter

  @Test
  fun `should create new goal for a prison number`() {
    // Given
    val goal = aValidGoal()
    given(persistenceAdapter.createGoal(any(), any())).willReturn(goal)

    val prisonNumber = aValidPrisonNumber()

    val createGoalDto = aValidCreateGoalDto()

    // When
    val actual = service.createGoal(prisonNumber, createGoalDto)

    // Then
    assertThat(actual).isEqualTo(goal)
    verify(persistenceAdapter).createGoal(prisonNumber, createGoalDto)
  }

  @Test
  fun `should get goal given goal exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    val goal = aValidGoal(reference = goalReference)
    given(persistenceAdapter.getGoal(any(), any())).willReturn(goal)

    // When
    val actual = service.getGoal(prisonNumber, goalReference)

    // Then
    assertThat(actual).isEqualTo(goal)
    verify(persistenceAdapter).getGoal(prisonNumber, goalReference)
  }

  @Test
  fun `should not get goal given goal does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    given(persistenceAdapter.getGoal(any(), any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(
      { service.getGoal(prisonNumber, goalReference) },
      GoalNotFoundException::class.java,
    )

    // Then
    assertThat(exception).hasMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    assertThat(exception.goalReference).isEqualTo(goalReference)
    verify(persistenceAdapter).getGoal(prisonNumber, goalReference)
  }

  @Test
  fun `should update goal given goal exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    val goal = aValidGoal(reference = goalReference)
    given(persistenceAdapter.updateGoal(any(), any())).willReturn(goal)

    val updatedGoal = aValidUpdateGoalDto(reference = goalReference)

    // When
    val actual = service.updateGoal(prisonNumber, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(goal)
    verify(persistenceAdapter).updateGoal(prisonNumber, updatedGoal)
  }

  @Test
  fun `should not update goal given goal does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    given(persistenceAdapter.updateGoal(any(), any())).willReturn(null)

    val updatedGoal = aValidUpdateGoalDto(reference = goalReference)

    // When
    val exception = catchThrowableOfType(
      { service.updateGoal(prisonNumber, updatedGoal) },
      GoalNotFoundException::class.java,
    )

    // Then
    assertThat(exception).hasMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    assertThat(exception.goalReference).isEqualTo(goalReference)
    verify(persistenceAdapter).updateGoal(prisonNumber, updatedGoal)
  }
}
