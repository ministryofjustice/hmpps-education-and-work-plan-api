package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult.ArchiveReasonIsOtherButNoDescriptionProvided
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult.ArchivedGoalSuccessfully
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult.GoalToBeArchivedCouldNotBeFound
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult.TriedToArchiveAGoalInAnInvalidState
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult.GoalToBeUnarchivedCouldNotBeFound
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult.TriedToUnarchiveAGoalInAnInvalidState
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult.UnArchivedGoalSuccessfully
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
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(null)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(GoalToBeArchivedCouldNotBeFound(prisonNumber, goalReference))
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
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(existingGoal)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(TriedToArchiveAGoalInAnInvalidState(prisonNumber, goalReference, status))
    }

    @ParameterizedTest
    @NullAndEmptySource
    fun `Should return an error if reason description is null or empty and reason was OTHER`(reasonOther: String?) {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal =
        aValidArchiveGoalDto(reference = goalReference, reason = ReasonToArchiveGoal.OTHER, reasonOther = reasonOther)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(existingGoal)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(ArchiveReasonIsOtherButNoDescriptionProvided(prisonNumber, goalReference))
    }

    @Test
    fun `should return an error if archiving the goal returns no goal`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(existingGoal)
      given(goalPersistenceAdapter.archiveGoal(prisonNumber, archiveGoal)).willReturn(null)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(GoalToBeArchivedCouldNotBeFound(prisonNumber, goalReference))
    }

    @Test
    fun `should return an updated goal if archiving the goal is successful`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      val archivedGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(existingGoal)
      given(goalPersistenceAdapter.archiveGoal(prisonNumber, archiveGoal)).willReturn(archivedGoal)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(ArchivedGoalSuccessfully(archivedGoal))
      verify(goalPersistenceAdapter).archiveGoal(prisonNumber, archiveGoal)
    }
  }

  @Nested
  inner class UnarchiveGoals {

    @Test
    fun `should return an error if goal to be unarchived can't be found`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(null)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(GoalToBeUnarchivedCouldNotBeFound(prisonNumber, goalReference))
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
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(existingGoal)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(TriedToUnarchiveAGoalInAnInvalidState(prisonNumber, goalReference, status))
    }

    @Test
    fun `should return an error if unarchiving the goal returns no goal`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(existingGoal)
      given(goalPersistenceAdapter.unarchiveGoal(prisonNumber, unarchiveGoal)).willReturn(null)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(GoalToBeUnarchivedCouldNotBeFound(prisonNumber, goalReference))
    }

    @Test
    fun `should return an updated goal if unarchiving the goal is successful`() {
      val prisonNumber = aValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      val unarchivedGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      given(goalPersistenceAdapter.getGoal(prisonNumber, goalReference)).willReturn(existingGoal)
      given(goalPersistenceAdapter.unarchiveGoal(prisonNumber, unarchiveGoal)).willReturn(unarchivedGoal)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(UnArchivedGoalSuccessfully(unarchivedGoal))
      verify(goalPersistenceAdapter).unarchiveGoal(prisonNumber, unarchiveGoal)
    }
  }
}
