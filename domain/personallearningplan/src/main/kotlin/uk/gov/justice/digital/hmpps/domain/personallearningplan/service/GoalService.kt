package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult.ArchiveReasonIsOtherButNoDescriptionProvided
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult.ArchivedGoalSuccessfully
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult.GoalToBeArchivedCouldNotBeFound
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult.TriedToArchiveAGoalInAnInvalidState
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult.GoalToBeUnarchivedCouldNotBeFound
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult.TriedToUnarchiveAGoalInAnInvalidState
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult.UnArchivedGoalSuccessfully
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
        .also { actionPlanEventService.actionPlanCreated(it) }
        .let { it.goals }
    } else {
      goalPersistenceAdapter.createGoals(prisonNumber, createGoalDtos)
        .also {
          goalEventService.goalsCreated(prisonNumber, it)
        }
    }
  }

  /**
   * Returns a [Goal] identified by its `prisonNumber` and `goalReference`.
   * Throws [GoalNotFoundException] if the [Goal] cannot be found.
   */
  fun getGoal(prisonNumber: String, goalReference: UUID): Goal {
    log.info { "Retrieving Goal with reference [$goalReference] for prisoner [$prisonNumber]" }
    return goalPersistenceAdapter.getGoal(prisonNumber, goalReference)
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
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
      }
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      }
  }

  /**
   * Archives a [Goal], identified by its `prisonNumber` and `goalReference`, from the specified [ArchiveGoalDto].
   *
   * Returns an [ArchiveGoalResult] from which the success of the operation can be determined.
   */
  fun archiveGoal(prisonNumber: String, archiveGoalDto: ArchiveGoalDto): ArchiveGoalResult {
    val goalReference = archiveGoalDto.reference
    log.info { "Archiving Goal with reference [$goalReference] for prisoner [$prisonNumber] because [${archiveGoalDto.reason}] with description [${archiveGoalDto.reasonOther ?: ""}]" }

    if (checkReasonIsSpecifiedIfOther(archiveGoalDto)) {
      return ArchiveReasonIsOtherButNoDescriptionProvided(
        prisonNumber,
        goalReference,
      )
    }

    val existingGoal = goalPersistenceAdapter.getGoal(prisonNumber, goalReference)
    return if (existingGoal == null) {
      GoalToBeArchivedCouldNotBeFound(prisonNumber, goalReference)
    } else if (existingGoal.status != GoalStatus.ACTIVE) {
      TriedToArchiveAGoalInAnInvalidState(
        prisonNumber,
        goalReference,
        existingGoal.status,
      )
    } else {
      goalPersistenceAdapter.archiveGoal(prisonNumber, archiveGoalDto)
        ?.also { goalEventService.goalArchived(prisonNumber, it) }
        ?.let { ArchivedGoalSuccessfully(it) }
        ?: GoalToBeArchivedCouldNotBeFound(prisonNumber, goalReference)
    }
  }

  /**
   * Unarchives a [Goal], identified by its `prisonNumber` and `goalReference`, from the specified [UnarchiveGoalDto].
   *
   * Returns an [UnarchiveGoalResult] from which the success of the operation can be determined.
   */
  fun unarchiveGoal(prisonNumber: String, unarchiveGoalDto: UnarchiveGoalDto): UnarchiveGoalResult {
    val goalReference = unarchiveGoalDto.reference
    log.info { "Unarchiving Goal with reference [$goalReference] for prisoner [$prisonNumber]." }

    val existingGoal = goalPersistenceAdapter.getGoal(prisonNumber, goalReference)

    return if (existingGoal == null) {
      GoalToBeUnarchivedCouldNotBeFound(prisonNumber, goalReference)
    } else if (existingGoal.status != GoalStatus.ARCHIVED) {
      TriedToUnarchiveAGoalInAnInvalidState(
        prisonNumber,
        goalReference,
        existingGoal.status,
      )
    } else {
      goalPersistenceAdapter.unarchiveGoal(prisonNumber, unarchiveGoalDto)
        ?.also { goalEventService.goalUnArchived(prisonNumber, it) }
        ?.let { UnArchivedGoalSuccessfully(it) }
        ?: GoalToBeUnarchivedCouldNotBeFound(prisonNumber, goalReference)
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
