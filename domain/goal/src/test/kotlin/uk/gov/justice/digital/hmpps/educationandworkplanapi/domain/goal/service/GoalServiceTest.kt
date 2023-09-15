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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidUpdateGoalDto

@ExtendWith(MockitoExtension::class)
class GoalServiceTest {
  @InjectMocks
  private lateinit var service: GoalService

  @Mock
  private lateinit var goalPersistenceAdapter: GoalPersistenceAdapter

  @Mock
  private lateinit var goalEventService: GoalEventService

  @Mock
  private lateinit var actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter

  @Mock
  private lateinit var actionPlanEventService: ActionPlanEventService

  @Test
  fun `should create new goal for a prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goal = aValidGoal()
    given(actionPlanPersistenceAdapter.getActionPlan(any())).willReturn(aValidActionPlan())
    given(goalPersistenceAdapter.createGoal(any(), any())).willReturn(goal)
    val createGoalDto = aValidCreateGoalDto()

    // When
    val actual = service.createGoal(prisonNumber, createGoalDto)

    // Then
    assertThat(actual).isEqualTo(goal)
    verify(actionPlanPersistenceAdapter).getActionPlan(prisonNumber)
    verify(goalPersistenceAdapter).createGoal(prisonNumber, createGoalDto)
    verify(goalEventService).goalCreated(prisonNumber, actual)
    verifyNoInteractions(actionPlanEventService)
  }

  @Test
  fun `should add goal to new action plan given prisoner does not have an action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goal = aValidGoal()
    val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal))
    given(actionPlanPersistenceAdapter.getActionPlan(any())).willReturn(null)
    given(actionPlanPersistenceAdapter.createActionPlan(any())).willReturn(actionPlan)
    val createGoalDto = aValidCreateGoalDto()
    val expectedCreateActionPlanDto =
      aValidCreateActionPlanDto(prisonNumber = prisonNumber, reviewDate = null, goals = listOf(createGoalDto))

    // When
    val actual = service.createGoal(prisonNumber, createGoalDto)

    // Then
    assertThat(actual).isEqualTo(goal)
    verify(actionPlanPersistenceAdapter).getActionPlan(prisonNumber)
    verify(actionPlanPersistenceAdapter).createActionPlan(expectedCreateActionPlanDto)
    verify(actionPlanEventService).actionPlanCreated(actionPlan)
    verifyNoInteractions(goalPersistenceAdapter)
    verifyNoInteractions(goalEventService)
  }

  @Test
  fun `should get goal given goal exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    val goal = aValidGoal(reference = goalReference)
    given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)

    // When
    val actual = service.getGoal(prisonNumber, goalReference)

    // Then
    assertThat(actual).isEqualTo(goal)
    verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
  }

  @Test
  fun `should not get goal given goal does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(null)

    // When
    val exception = catchThrowableOfType(
      { service.getGoal(prisonNumber, goalReference) },
      GoalNotFoundException::class.java,
    )

    // Then
    assertThat(exception).hasMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    assertThat(exception.goalReference).isEqualTo(goalReference)
    verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
  }

  @Test
  fun `should update goal given goal exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    val goal = aValidGoal(reference = goalReference)
    given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)
    given(goalPersistenceAdapter.updateGoal(any(), any())).willReturn(goal)

    val updatedGoal = aValidUpdateGoalDto(reference = goalReference)

    // When
    val actual = service.updateGoal(prisonNumber, updatedGoal)

    // Then
    assertThat(actual).isEqualTo(goal)
    verify(goalPersistenceAdapter).updateGoal(prisonNumber, updatedGoal)
    verify(goalEventService).goalUpdated(prisonNumber, goal, actual)
  }

  @Test
  fun `should not update goal given goal does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goalReference = aValidReference()

    given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(null)

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
    verify(goalPersistenceAdapter, never()).updateGoal(prisonNumber, updatedGoal)
    verifyNoInteractions(goalEventService)
  }
}
