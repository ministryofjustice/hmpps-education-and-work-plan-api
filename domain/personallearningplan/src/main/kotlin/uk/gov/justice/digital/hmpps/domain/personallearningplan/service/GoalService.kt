package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalAction
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.InvalidGoalStateException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.NoArchiveReasonException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.PrisonerHasNoGoalsException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules for the Goal domain, and is how applications
 * must create and manage [Goal]s.
 *
 * Applications using [Goal]s must new up an instance of this class providing an implementation of
 * [GoalPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain. Service method behaviour can however be customized and extended by using the [GoalEventService].
 *
 */
class GoalService(
  private val goalPersistenceAdapter: GoalPersistenceAdapter,
  private val goalEventService: GoalEventService,
  private val actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
  private val actionPlanEventService: ActionPlanEventService,
  private val goalNotesService: GoalNotesService,
) {

  /**
   * Creates a new [Goal] for the prisoner identified by their prison number, with the data in the specified [CreateGoalDto]
   */
  fun createGoal(prisonNumber: String, createGoalDto: CreateGoalDto): Goal {
    return createGoals(prisonNumber, listOf(createGoalDto))[0]
  }

  /**
   * Creates new [Goal]s for the prisoner identified by their prison number, with the goal data in the specified list of [CreateGoalDto]s
   */
  fun createGoals(prisonNumber: String, createGoalDtos: List<CreateGoalDto>): List<Goal> {
    log.info { "Creating new ${if (createGoalDtos.size == 1) "Goal" else "Goals"} for prisoner [$prisonNumber]" }

    // TODO RR-227 - We need to change throw a 404 once the create action plan endpoint is being called by the UI (with an optional review date)
    return if (actionPlanDoesNotExist(prisonNumber)) {
      actionPlanPersistenceAdapter.createActionPlan(newActionPlan(prisonNumber, createGoalDtos))
        .apply {
          actionPlanEventService.actionPlanCreated(this)
          goalNotesService.createNotes(prisonNumber, goals)
        }.goals
    } else {
      goalPersistenceAdapter.createGoals(prisonNumber, createGoalDtos).apply {
        goalNotesService.createNotes(prisonNumber, this)
        goalEventService.goalsCreated(prisonNumber, this)
      }
    }
  }

  fun getGoals(getGoalsDto: GetGoalsDto): List<Goal> {
    return goalPersistenceAdapter.getGoals(getGoalsDto.prisonNumber)
      ?.filter { getGoalsDto.statuses.isNullOrEmpty() || it.status in getGoalsDto.statuses }
      ?.onEach { it.notes = goalNotesService.getNotes(it.reference) }
      ?: run {
        log.info { "No goals have been created for prisoner [${getGoalsDto.prisonNumber}] yet" }
        throw PrisonerHasNoGoalsException(getGoalsDto.prisonNumber)
      }
  }

  /**
   * Returns a [Goal] identified by its `prisonNumber` and `goalReference`.
   * Throws [GoalNotFoundException] if the [Goal] cannot be found.
   */
  fun getGoal(prisonNumber: String, goalReference: UUID): Goal {
    log.info { "Retrieving Goal with reference [$goalReference] for prisoner [$prisonNumber]" }
    return goalPersistenceAdapter.getGoal(prisonNumber, goalReference)?.apply {
      notes = goalNotesService.getNotes(reference)
    } ?: run {
      log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      throw GoalNotFoundException(prisonNumber, goalReference)
    }
  }

  /**
   * Updates a [Goal], identified by its `prisonNumber` and `goalReference`, from the specified [UpdateGoalDto].
   * Throws [GoalNotFoundException] if the [Goal] to be updated cannot be found.
   */
  fun updateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto): Goal {
    val goalReference = updatedGoalDto.reference
    log.info { "Updating Goal with reference [$goalReference] for prisoner [$prisonNumber]" }

    val existingGoal = getGoal(prisonNumber, updatedGoalDto.reference)
    return goalPersistenceAdapter.updateGoal(prisonNumber, updatedGoalDto)
      ?.also {
        goalEventService.goalUpdated(prisonNumber = prisonNumber, previousGoal = existingGoal, updatedGoal = it)
        updateNote(prisonNumber, it, updatedGoalDto.notes)
      }
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      }
  }

  // if the note is different to the note before then update/create the note
  private fun updateNote(prisonNumber: String, goal: Goal, updatedNote: String?) {
    val existingNote = goalNotesService.getNotes(goal.reference)

    when {
      existingNote == null && updatedNote != null -> {
        goalNotesService.createNotes(prisonNumber, listOf(goal))
      }
      existingNote != updatedNote && updatedNote != null -> {
        if (updatedNote.isEmpty()) {
          goalNotesService.deleteNote(goal.reference)
        } else {
          goalNotesService.updateNotes(goal.reference, goal.lastUpdatedAtPrison, updatedNote)
        }
      }
      // No action if the note hasn't changed or both are null
    }
  }

  /**
   * Archives a [Goal], identified by its `prisonNumber` and `goalReference`, from the specified [ArchiveGoalDto].
   *
   * Returns the archived [Goal]
   */
  fun archiveGoal(prisonNumber: String, archiveGoalDto: ArchiveGoalDto): Goal {
    val goalReference = archiveGoalDto.reference
    log.info { "Archiving Goal with reference [$goalReference] for prisoner [$prisonNumber] because [${archiveGoalDto.reason}] with description [${archiveGoalDto.reasonOther ?: ""}]" }

    if (checkReasonIsSpecifiedIfOther(archiveGoalDto)) {
      throw NoArchiveReasonException(goalReference, prisonNumber, ReasonToArchiveGoal.OTHER)
    }

    val existingGoal = goalPersistenceAdapter.getGoal(prisonNumber, goalReference)
    return if (existingGoal == null) {
      throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      }
    } else if (existingGoal.status != GoalStatus.ACTIVE) {
      throw InvalidGoalStateException(
        prisonNumber,
        goalReference,
        existingGoal.status,
        GoalAction.ARCHIVE,
      )
    } else {
      goalPersistenceAdapter.archiveGoal(prisonNumber, archiveGoalDto)
        ?.also { goalEventService.goalArchived(prisonNumber, it) }
        ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
          log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
        }
    }
  }

  /**
   * Unarchives a [Goal], identified by its `prisonNumber` and `goalReference`, from the specified [UnarchiveGoalDto].
   *
   * Returns the unarchived [Goal]
   */
  fun unarchiveGoal(prisonNumber: String, unarchiveGoalDto: UnarchiveGoalDto): Goal {
    val goalReference = unarchiveGoalDto.reference
    log.info { "Unarchiving Goal with reference [$goalReference] for prisoner [$prisonNumber]." }

    val existingGoal = goalPersistenceAdapter.getGoal(prisonNumber, goalReference)
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found." }
      }

    if (existingGoal.status != GoalStatus.ARCHIVED) {
      throw InvalidGoalStateException(
        prisonNumber,
        goalReference,
        existingGoal.status,
        GoalAction.UNARCHIVE,
      )
    }

    return goalPersistenceAdapter.unarchiveGoal(prisonNumber, unarchiveGoalDto)
      ?.also {
        goalEventService.goalUnArchived(prisonNumber, it)
      } ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
      log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found after unarchive attempt." }
    }
  }

  private fun checkReasonIsSpecifiedIfOther(archiveGoalDto: ArchiveGoalDto) =
    archiveGoalDto.reason == ReasonToArchiveGoal.OTHER && archiveGoalDto.reasonOther.isNullOrEmpty()

  private fun actionPlanDoesNotExist(prisonNumber: String) =
    actionPlanPersistenceAdapter.getActionPlan(prisonNumber) == null

  private fun newActionPlan(prisonNumber: String, createGoalDtos: List<CreateGoalDto>) =
    CreateActionPlanDto(
      prisonNumber = prisonNumber,
      reviewDate = null,
      goals = createGoalDtos,
    )
}
