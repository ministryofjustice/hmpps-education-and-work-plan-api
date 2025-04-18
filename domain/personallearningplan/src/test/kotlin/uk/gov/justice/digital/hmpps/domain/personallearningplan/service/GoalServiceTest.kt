package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Assertions
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.InvalidGoalStateException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.NoArchiveReasonException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.PrisonerHasNoGoalsException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateGoalDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber

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
  private lateinit var goalNotesService: GoalNotesService

  @Nested
  inner class CreateGoal {
    @Test
    fun `should create new goal for a prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
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
    }

    @Test
    fun `should not add goal to action plan given prisoner does not have an action plan`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      given(actionPlanPersistenceAdapter.getActionPlan(any())).willReturn(null)
      val createGoalDto = aValidCreateGoalDto()

      // When
      val exception = catchThrowableOfType(ActionPlanNotFoundException::class.java) {
        service.createGoal(prisonNumber, createGoalDto)
      }

      // Then
      assertThat(exception)
        .hasMessage("ActionPlan for prisoner [$prisonNumber] not found")
      verify(actionPlanPersistenceAdapter).getActionPlan(prisonNumber)
      verifyNoMoreInteractions(actionPlanPersistenceAdapter)
      verifyNoInteractions(goalPersistenceAdapter)
      verifyNoInteractions(goalEventService)
    }
  }

  @Nested
  inner class CreateGoals {
    @Test
    fun `should create new goals for a prisoner`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
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
    }

    @Test
    fun `should add goals to new action plan given prisoner does not have an action plan`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      given(actionPlanPersistenceAdapter.getActionPlan(any())).willReturn(null)

      val createGoalDto1 = aValidCreateGoalDto(title = "Goal 1")
      val createGoalDto2 = aValidCreateGoalDto(title = "Goal 2")
      val createGoalDtos = listOf(createGoalDto1, createGoalDto2)

      // When
      val exception = catchThrowableOfType(ActionPlanNotFoundException::class.java) {
        service.createGoals(prisonNumber, createGoalDtos)
      }

      // Then
      assertThat(exception)
        .hasMessage("ActionPlan for prisoner [$prisonNumber] not found")
      verify(actionPlanPersistenceAdapter).getActionPlan(prisonNumber)
      verifyNoMoreInteractions(actionPlanPersistenceAdapter)
      verifyNoInteractions(goalPersistenceAdapter)
      verifyNoInteractions(goalEventService)
    }
  }

  @Nested
  inner class GetGoal {
    @Test
    fun `should get goal given goal exists`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()
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
  }

  @Nested
  inner class UpdateGoal {
    @Test
    fun `should update goal given goal exists`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()
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
      verifyNoInteractions(goalNotesService)
      verifyNoInteractions(goalEventService)
    }

    @Nested
    inner class UpdateGoalNotes {
      @Test
      fun `should update goal with updated notes given different note content was previously on the goal`() {
        // Given
        val prisonNumber = randomValidPrisonNumber()
        val goalReference = aValidReference()

        val goal = aValidGoal(reference = goalReference)
        given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)
        given(goalPersistenceAdapter.updateGoal(any(), any())).willReturn(goal)

        val existingNote = "The previous goal note content"
        given(goalNotesService.getNotes(any())).willReturn(existingNote)

        val updatedGoal = aValidUpdateGoalDto(
          reference = goalReference,
          notes = "The new note content",
        )

        // When
        val actual = service.updateGoal(prisonNumber, updatedGoal)

        // Then
        assertThat(actual).isEqualTo(goal)
        verify(goalPersistenceAdapter).updateGoal(prisonNumber, updatedGoal)
        verify(goalNotesService, times(2)).getNotes(goalReference)
        verify(goalNotesService).updateNotes(goalReference, goal.lastUpdatedAtPrison, "The new note content")
        verify(goalEventService).goalUpdated(prisonNumber, goal, actual)
      }

      @Test
      fun `should update goal with new notes given no note content was previously on the goal`() {
        // Given
        val prisonNumber = randomValidPrisonNumber()
        val goalReference = aValidReference()

        val goal = aValidGoal(reference = goalReference)
        given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)
        given(goalPersistenceAdapter.updateGoal(any(), any())).willReturn(goal)

        val existingNote = null
        given(goalNotesService.getNotes(any())).willReturn(existingNote)

        val updatedGoal = aValidUpdateGoalDto(
          reference = goalReference,
          notes = "The new note content",
        )

        // When
        val actual = service.updateGoal(prisonNumber, updatedGoal)

        // Then
        assertThat(actual).isEqualTo(goal)
        verify(goalPersistenceAdapter).updateGoal(prisonNumber, updatedGoal)
        verify(goalNotesService, times(2)).getNotes(goalReference)
        verify(goalNotesService).createNotes(prisonNumber, listOf(goal))
        verify(goalEventService).goalUpdated(prisonNumber, goal, actual)
      }

      @Test
      fun `should update goal by deleting notes given notes on the goal and empty string notes in the request`() {
        // Given
        val prisonNumber = randomValidPrisonNumber()
        val goalReference = aValidReference()

        val goal = aValidGoal(reference = goalReference)
        given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)
        given(goalPersistenceAdapter.updateGoal(any(), any())).willReturn(goal)

        val existingNote = "The existing goal note"
        given(goalNotesService.getNotes(any())).willReturn(existingNote)

        val updatedGoal = aValidUpdateGoalDto(
          reference = goalReference,
          notes = "",
        )

        // When
        val actual = service.updateGoal(prisonNumber, updatedGoal)

        // Then
        assertThat(actual).isEqualTo(goal)
        verify(goalPersistenceAdapter).updateGoal(prisonNumber, updatedGoal)
        verify(goalNotesService, times(2)).getNotes(goalReference)
        verify(goalNotesService).deleteNote(goalReference)
        verify(goalEventService).goalUpdated(prisonNumber, goal, actual)
      }

      @Test
      fun `should update goal by deleting notes given notes on the goal and null notes in the request`() {
        // Given
        val prisonNumber = randomValidPrisonNumber()
        val goalReference = aValidReference()

        val goal = aValidGoal(reference = goalReference)
        given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)
        given(goalPersistenceAdapter.updateGoal(any(), any())).willReturn(goal)

        val existingNote = "The existing goal note"
        given(goalNotesService.getNotes(any())).willReturn(existingNote)

        val updatedGoal = aValidUpdateGoalDto(
          reference = goalReference,
          notes = null,
        )

        // When
        val actual = service.updateGoal(prisonNumber, updatedGoal)

        // Then
        assertThat(actual).isEqualTo(goal)
        verify(goalPersistenceAdapter).updateGoal(prisonNumber, updatedGoal)
        verify(goalNotesService, times(2)).getNotes(goalReference)
        verify(goalNotesService).deleteNote(goalReference)
        verify(goalEventService).goalUpdated(prisonNumber, goal, actual)
      }

      @Test
      fun `should update goal but not update notes given notes on the goal are the same as notes in the request`() {
        // Given
        val prisonNumber = randomValidPrisonNumber()
        val goalReference = aValidReference()

        val goal = aValidGoal(reference = goalReference)
        given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)
        given(goalPersistenceAdapter.updateGoal(any(), any())).willReturn(goal)

        val existingNote = "The goal notes"
        given(goalNotesService.getNotes(any())).willReturn(existingNote)

        val updatedGoal = aValidUpdateGoalDto(
          reference = goalReference,
          notes = "The goal notes",
        )

        // When
        val actual = service.updateGoal(prisonNumber, updatedGoal)

        // Then
        assertThat(actual).isEqualTo(goal)
        verify(goalPersistenceAdapter).updateGoal(prisonNumber, updatedGoal)
        verify(goalNotesService, times(2)).getNotes(goalReference)
        verifyNoMoreInteractions(goalNotesService)
        verify(goalEventService).goalUpdated(prisonNumber, goal, actual)
      }

      @Test
      fun `should update goal but not update notes given no notes on the goal and null notes in the request`() {
        // Given
        val prisonNumber = randomValidPrisonNumber()
        val goalReference = aValidReference()

        val goal = aValidGoal(reference = goalReference)
        given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)
        given(goalPersistenceAdapter.updateGoal(any(), any())).willReturn(goal)

        val existingNote = null
        given(goalNotesService.getNotes(any())).willReturn(existingNote)

        val updatedGoal = aValidUpdateGoalDto(
          reference = goalReference,
          notes = null,
        )

        // When
        val actual = service.updateGoal(prisonNumber, updatedGoal)

        // Then
        assertThat(actual).isEqualTo(goal)
        verify(goalPersistenceAdapter).updateGoal(prisonNumber, updatedGoal)
        verify(goalNotesService, times(2)).getNotes(goalReference)
        verifyNoMoreInteractions(goalNotesService)
        verify(goalEventService).goalUpdated(prisonNumber, goal, actual)
      }

      @Test
      fun `should update goal but not update notes given no notes on the goal and empty string notes in the request`() {
        // Given
        val prisonNumber = randomValidPrisonNumber()
        val goalReference = aValidReference()

        val goal = aValidGoal(reference = goalReference)
        given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(goal)
        given(goalPersistenceAdapter.updateGoal(any(), any())).willReturn(goal)

        val existingNote = null
        given(goalNotesService.getNotes(any())).willReturn(existingNote)

        val updatedGoal = aValidUpdateGoalDto(
          reference = goalReference,
          notes = "",
        )

        // When
        val actual = service.updateGoal(prisonNumber, updatedGoal)

        // Then
        assertThat(actual).isEqualTo(goal)
        verify(goalPersistenceAdapter).updateGoal(prisonNumber, updatedGoal)
        verify(goalNotesService, times(2)).getNotes(goalReference)
        verifyNoMoreInteractions(goalNotesService)
        verify(goalEventService).goalUpdated(prisonNumber, goal, actual)
      }
    }
  }

  @Nested
  inner class ArchiveGoals {

    @Test
    fun `should return an error if goal to be archived can't be found`() {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(goalReference)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(null)

      val exception = Assertions.assertThrows(GoalNotFoundException::class.java) {
        service.archiveGoal(prisonNumber, archiveGoal)
      }

      // Then
      assertThat(exception).hasMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
      verifyNoInteractions(goalEventService)
    }

    @ParameterizedTest
    @CsvSource(
      "ARCHIVED",
      "COMPLETED",
    )
    fun `should return an error if goal to be archived is in an invalid state`(status: GoalStatus) {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(reference = goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = status)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)

      val exception = Assertions.assertThrows(InvalidGoalStateException::class.java) {
        service.archiveGoal(prisonNumber, archiveGoal)
      }

      // Then
      assertThat(exception).hasMessage("Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [$status] that can't be archived")
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verifyNoInteractions(goalEventService)
    }

    @ParameterizedTest
    @NullAndEmptySource
    fun `Should return an error if reason description is null or empty and reason was OTHER`(reasonOther: String?) {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal =
        aValidArchiveGoalDto(reference = goalReference, reason = ReasonToArchiveGoal.OTHER, reasonOther = reasonOther)

      val exception = Assertions.assertThrows(NoArchiveReasonException::class.java) {
        service.archiveGoal(prisonNumber, archiveGoal)
      }

      // Then
      assertThat(exception).hasMessage("Could not archive goal with reference [$goalReference] for prisoner [$prisonNumber]: Archive reason is ${ReasonToArchiveGoal.OTHER} but no description provided")
      verifyNoInteractions(goalEventService)
      verifyNoInteractions(goalPersistenceAdapter)
      verifyNoInteractions(goalEventService)
    }

    @Test
    fun `should return an error if archiving the goal returns no goal`() {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)
      given(goalPersistenceAdapter.archiveGoal(any(), any())).willReturn(null)

      val exception = Assertions.assertThrows(GoalNotFoundException::class.java) {
        service.archiveGoal(prisonNumber, archiveGoal)
      }

      // Then
      assertThat(exception).hasMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verify(goalPersistenceAdapter).archiveGoal(prisonNumber, archiveGoal)
      verifyNoInteractions(goalEventService)
    }

    @Test
    fun `should return an updated goal if archiving the goal is successful`() {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val archiveGoal = aValidArchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      val archivedGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)
      given(goalPersistenceAdapter.archiveGoal(any(), any())).willReturn(archivedGoal)

      val result = service.archiveGoal(prisonNumber, archiveGoal)

      assertThat(result).isEqualTo(archivedGoal)
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verify(goalPersistenceAdapter).archiveGoal(prisonNumber, archiveGoal)
      verify(goalEventService).goalArchived(prisonNumber, archivedGoal)
    }
  }

  @Nested
  inner class UnarchiveGoals {

    @Test
    fun `should return an error if goal to be unarchived can't be found`() {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(null)

      val exception = Assertions.assertThrows(GoalNotFoundException::class.java) {
        service.unarchiveGoal(prisonNumber, unarchiveGoal)
      }

      // Then
      assertThat(exception).hasMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verifyNoInteractions(goalEventService)
    }

    @ParameterizedTest
    @CsvSource(
      "ACTIVE",
      "COMPLETED",
    )
    fun `should return an error if goal to be unarchived is in an invalid state`(status: GoalStatus) {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(reference = goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = status)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)

      val exception = Assertions.assertThrows(InvalidGoalStateException::class.java) {
        service.unarchiveGoal(prisonNumber, unarchiveGoal)
      }

      assertThat(exception).hasMessage("Could not unarchive goal with reference [$goalReference] for prisoner [$prisonNumber]: Goal was in state [$status] that can't be unarchived")
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verifyNoInteractions(goalEventService)
    }

    @Test
    fun `should return an error if unarchiving the goal returns no goal`() {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)
      given(goalPersistenceAdapter.unarchiveGoal(any(), any())).willReturn(null)

      val exception = Assertions.assertThrows(GoalNotFoundException::class.java) {
        service.unarchiveGoal(prisonNumber, unarchiveGoal)
      }

      assertThat(exception).hasMessage("Goal with reference [$goalReference] for prisoner [$prisonNumber] not found")
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verify(goalPersistenceAdapter).unarchiveGoal(prisonNumber, unarchiveGoal)
      verifyNoInteractions(goalEventService)
    }

    @Test
    fun `should return an updated goal if unarchiving the goal is successful`() {
      val prisonNumber = randomValidPrisonNumber()
      val goalReference = aValidReference()
      val unarchiveGoal = aValidUnarchiveGoalDto(goalReference)
      val existingGoal = aValidGoal(reference = goalReference, status = GoalStatus.ARCHIVED)
      val unarchivedGoal = aValidGoal(reference = goalReference, status = GoalStatus.ACTIVE)
      given(goalPersistenceAdapter.getGoal(any(), any())).willReturn(existingGoal)
      given(goalPersistenceAdapter.unarchiveGoal(any(), any())).willReturn(unarchivedGoal)

      val result = service.unarchiveGoal(prisonNumber, unarchiveGoal)

      assertThat(result).isEqualTo(unarchivedGoal)
      verify(goalPersistenceAdapter).getGoal(prisonNumber, goalReference)
      verify(goalPersistenceAdapter).unarchiveGoal(prisonNumber, unarchiveGoal)
      verify(goalEventService).goalUnArchived(prisonNumber, unarchivedGoal)
    }
  }

  @Nested
  inner class GetGoals {
    private val prisonNumber = randomValidPrisonNumber()
    private val activeGoal = aValidGoal(reference = aValidReference(), status = GoalStatus.ACTIVE)
    private val archivedGoal = aValidGoal(reference = aValidReference(), status = GoalStatus.ARCHIVED)
    private val completedGoal = aValidGoal(reference = aValidReference(), status = GoalStatus.COMPLETED)

    @Test
    fun `should return all goals if no filter`() {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(listOf(activeGoal, archivedGoal, completedGoal))

      // When
      val goals = service.getGoals(GetGoalsDto(prisonNumber, null))

      // Then
      assertThat(goals).isEqualTo(
        listOf(
          activeGoal,
          archivedGoal,
          completedGoal,
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
        listOf(
          activeGoal,
          archivedGoal,
          completedGoal,
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
      val goals = service.getGoals(GetGoalsDto(prisonNumber, setOf(status)))

      // Then
      assertThat(goals).hasSize(1)
      assertThat(goals[0].status).isEqualTo(status)
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }

    @Test
    fun `should return only goals matching status if a filter is set with multiple statuses`() {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(listOf(activeGoal, archivedGoal, completedGoal))

      // When
      val goals = service.getGoals(GetGoalsDto(prisonNumber, setOf(GoalStatus.ACTIVE, GoalStatus.COMPLETED)))

      assertThat(goals).hasSize(2)
      assertThat(goals[0].reference).isEqualTo(activeGoal.reference)
      assertThat(goals[1].reference).isEqualTo(completedGoal.reference)
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }

    @Test
    fun `should return an empty list if the prisoner has no matching goals`() {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(emptyList())

      // When
      val actual = service.getGoals(GetGoalsDto(prisonNumber, null))

      // Then
      assertThat(actual).isEmpty()
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }

    @Test
    fun `should return a problem if the prisoner never had a plan created`() {
      // Given
      given(goalPersistenceAdapter.getGoals(any())).willReturn(null)

      // When
      val exception = Assertions.assertThrows(PrisonerHasNoGoalsException::class.java) {
        service.getGoals(GetGoalsDto(prisonNumber, null))
      }
      // Then
      assertThat(exception).hasMessage("No goals have been created for prisoner [$prisonNumber] yet")
      verify(goalPersistenceAdapter).getGoals(prisonNumber)
    }
  }
}
