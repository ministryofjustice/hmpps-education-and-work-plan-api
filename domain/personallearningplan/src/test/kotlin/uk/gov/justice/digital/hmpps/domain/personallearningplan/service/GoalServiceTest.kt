package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateActionPlanDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateGoalDto

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

  @Nested
  inner class CreateGoal {
    @Test
    fun `should create new goal for a prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val goal = aValidGoal()
      given(actionPlanPersistenceAdapter.getActionPlan(any())).willReturn(aValidActionPlan())

      val createdGoals = listOf(goal)
      given(goalPersistenceAdapter.createGoals(any(), any())).willReturn(createdGoals)
      val createGoalDto = aValidCreateGoalDto()

      // When
      val actual = service.createGoal(prisonNumber, createGoalDto)

      // Then
      assertThat(actual).isEqualTo(goal)
      verify(actionPlanPersistenceAdapter).getActionPlan(prisonNumber)
      verify(goalPersistenceAdapter).createGoals(prisonNumber, listOf(createGoalDto))
      verify(goalEventService).goalsCreated(prisonNumber, createdGoals)
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
  }

  @Nested
  inner class CreateGoals {
    @Test
    fun `should create new goals for a prisoner`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val goal1 = aValidGoal(title = "Goal 1")
      val goal2 = aValidGoal(title = "Goal 2")
      given(actionPlanPersistenceAdapter.getActionPlan(any())).willReturn(aValidActionPlan())

      val createdGoals = listOf(goal1, goal2)
      given(goalPersistenceAdapter.createGoals(any(), any())).willReturn(createdGoals)

      val createGoalDto1 = aValidCreateGoalDto(title = "Goal 1")
      val createGoalDto2 = aValidCreateGoalDto(title = "Goal 2")
      val createGoalDtos = listOf(createGoalDto1, createGoalDto2)

      // When
      val actual = service.createGoals(prisonNumber, createGoalDtos)

      // Then
      assertThat(actual).containsExactlyInAnyOrder(goal1, goal2)
      verify(actionPlanPersistenceAdapter).getActionPlan(prisonNumber)
      verify(goalPersistenceAdapter).createGoals(prisonNumber, createGoalDtos)
      verify(goalEventService).goalsCreated(prisonNumber, createdGoals)
      verifyNoInteractions(actionPlanEventService)
    }

    @Test
    fun `should add goals to new action plan given prisoner does not have an action plan`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val goal1 = aValidGoal(title = "Goal 1")
      val goal2 = aValidGoal(title = "Goal 2")
      val actionPlan = aValidActionPlan(prisonNumber = prisonNumber, goals = listOf(goal1, goal2))
      given(actionPlanPersistenceAdapter.getActionPlan(any())).willReturn(null)
      given(actionPlanPersistenceAdapter.createActionPlan(any())).willReturn(actionPlan)

      val createGoalDto1 = aValidCreateGoalDto(title = "Goal 1")
      val createGoalDto2 = aValidCreateGoalDto(title = "Goal 2")
      val createGoalDtos = listOf(createGoalDto1, createGoalDto2)

      val expectedCreateActionPlanDto =
        aValidCreateActionPlanDto(prisonNumber = prisonNumber, reviewDate = null, goals = createGoalDtos)

      // When
      val actual = service.createGoals(prisonNumber, createGoalDtos)

      // Then
      assertThat(actual).containsExactlyInAnyOrder(goal1, goal2)
      verify(actionPlanPersistenceAdapter).getActionPlan(prisonNumber)
      verify(actionPlanPersistenceAdapter).createActionPlan(expectedCreateActionPlanDto)
      verify(actionPlanEventService).actionPlanCreated(actionPlan)
      verifyNoInteractions(goalPersistenceAdapter)
      verifyNoInteractions(goalEventService)
    }
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
    val exception =
      catchThrowableOfType(GoalNotFoundException::class.java) { service.getGoal(prisonNumber, goalReference) }

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
    val exception =
      catchThrowableOfType(GoalNotFoundException::class.java) { service.updateGoal(prisonNumber, updatedGoal) }

    // Then
    assertThat(exception).hasMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
    assertThat(exception.prisonNumber).isEqualTo(prisonNumber)
    assertThat(exception.goalReference).isEqualTo(goalReference)
    verify(goalPersistenceAdapter, never()).updateGoal(prisonNumber, updatedGoal)
    verifyNoInteractions(goalEventService)
  }

  @Nested
  inner class ArchiveGoals {

    @Test
    fun `should return an error if goal to be archived can't be found`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(goalReference)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(null)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(ArchiveGoalResult.GoalNotFound(prisonNumber, goalReference))
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verifyNoInteractions(goalEventService)
    }

    @ParameterizedTest
    @CsvSource(
      "ARCHIVED",
      "COMPLETED",
    )
    fun `should return an error if goal to be archived is in an invalid state`(status: GoalStatus) {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(reference = goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = status)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(ArchiveGoalResult.GoalInAnInvalidState(prisonNumber, goalReference, status))
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verifyNoInteractions(goalEventService)
    }

    @ParameterizedTest
    @NullAndEmptySource
    fun `Should return an error if reason description is null or empty and reason was OTHER`(reasonOther: String?) {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal =
        aValidArchiveGoalDto(reference = goalReference, reason = ReasonToArchiveGoal.OTHER, reasonOther = reasonOther)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(ArchiveGoalResult.NoDescriptionProvidedForOther(prisonNumber, goalReference))
      verifyNoInteractions(goalPersistenceAdapter)
      verifyNoInteractions(goalEventService)
    }

    @Test
    fun `should return an error if archiving the goal returns no goal`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)
      given(goalPersistenceAdapter.archiveGoal(any(), any())).willReturn(null)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(ArchiveGoalResult.GoalNotFound(prisonNumber, goalReference))
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verify(goalPersistenceAdapter).archiveGoal(prisonNumber, archiveGoal)
      verifyNoInteractions(goalEventService)
    }

    @Test
    fun `should return an updated goal if archiving the goal is successful`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      val archivedGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)
      given(goalPersistenceAdapter.archiveGoal(any(), any())).willReturn(archivedGoal)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(ArchiveGoalResult.Success(archivedGoal))
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verify(goalPersistenceAdapter).archiveGoal(prisonNumber, archiveGoal)
      verify(goalEventService).goalArchived(prisonNumber, archivedGoal)
    }
  }

  @Nested
  inner class UnarchiveGoals {

    @Test
    fun `should return an error if goal to be unarchived can't be found`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(null)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(UnarchiveGoalResult.GoalNotFound(prisonNumber, goalReference))
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verifyNoInteractions(goalEventService)
    }

    @ParameterizedTest
    @CsvSource(
      "ACTIVE",
      "COMPLETED",
    )
    fun `should return an error if goal to be unarchived is in an invalid state`(status: GoalStatus) {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(reference = goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = status)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(UnarchiveGoalResult.GoalInAnInvalidState(prisonNumber, goalReference, status))
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verifyNoInteractions(goalEventService)
    }

    @Test
    fun `should return an error if unarchiving the goal returns no goal`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)
      given(goalPersistenceAdapter.unarchiveGoal(any(), any())).willReturn(null)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(UnarchiveGoalResult.GoalNotFound(prisonNumber, goalReference))
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verify(goalPersistenceAdapter).unarchiveGoal(prisonNumber, unarchiveGoal)
      verifyNoInteractions(goalEventService)
    }

    @Test
    fun `should return an updated goal if unarchiving the goal is successful`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      val unarchivedGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)
      given(goalPersistenceAdapter.unarchiveGoal(any(), any())).willReturn(unarchivedGoal)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(UnarchiveGoalResult.Success(unarchivedGoal))
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verify(goalPersistenceAdapter).unarchiveGoal(prisonNumber, unarchiveGoal)
      verify(goalEventService).goalUnArchived(prisonNumber, unarchivedGoal)
    }
  }

  @Nested
  inner class GetGoals {
    private val prisonNumber = aValidPrisonNumber()
    private val activeGoal = aValidGoal(reference = aValidReference(), status = GoalStatus.ACTIVE)
    private val archivedGoal = aValidGoal(reference = aValidReference(), status = GoalStatus.ARCHIVED)
    private val completedGoal = aValidGoal(reference = aValidReference(), status = GoalStatus.COMPLETED)

    @Test
    fun `should return all goals if no filter`() {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(listOf(activeGoal, archivedGoal, completedGoal))

      // When
      val actual = service.getGoals(GetGoalsDto(prisonNumber, null))

      // Then
      assertThat(actual).isEqualTo(
        GetGoalsResult.Success(
          listOf(
            activeGoal,
            archivedGoal,
            completedGoal,
          ),
        ),
      )
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }

    @Test
    fun `should return all goals given empty filter`() {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(listOf(activeGoal, archivedGoal, completedGoal))

      // When
      val actual = service.getGoals(GetGoalsDto(prisonNumber, emptySet()))

      // Then
      assertThat(actual).isEqualTo(
        GetGoalsResult.Success(
          listOf(
            activeGoal,
            archivedGoal,
            completedGoal,
          ),
        ),
      )
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }

    @ParameterizedTest
    @EnumSource(GoalStatus::class)
    fun `should return only goals matching status if a filter is set with a single status`(status: GoalStatus) {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(listOf(activeGoal, archivedGoal, completedGoal))

      // When
      val actual = service.getGoals(GetGoalsDto(prisonNumber, setOf(status)))

      // Then
      assertThat(actual).isInstanceOf(GetGoalsResult.Success::class.java)
      val goals = (actual as GetGoalsResult.Success).goals
      assertThat(goals).hasSize(1)
      assertThat(goals[0].status).isEqualTo(status)
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }

    @Test
    fun `should return an empty list if the prisoner has no matching goals`() {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(emptyList())

      // When
      val actual = service.getGoals(GetGoalsDto(prisonNumber, null))

      // Then
      assertThat(actual).isEqualTo(GetGoalsResult.Success(emptyList()))
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }

    @Test
    fun `should return a problem if the prisoner never had a plan created`() {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(null)

      // When
      val actual = service.getGoals(GetGoalsDto(prisonNumber, null))

      // Then
      assertThat(actual).isEqualTo(GetGoalsResult.PrisonerNotFound(prisonNumber))
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }
  }
}
