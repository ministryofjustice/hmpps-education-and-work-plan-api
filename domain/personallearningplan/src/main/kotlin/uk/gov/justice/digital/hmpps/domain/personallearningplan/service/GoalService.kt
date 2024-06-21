package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalProblem
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveReasonIsOtherButNoDescriptionProvided
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateActionPlanDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GoalToBeArchivedCouldNotBeFound
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.TriedToArchiveAGoalInAnInvalidState
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
   * Throws [GoalNotFoundException] if the [Goal] to be archived cannot be found.
   */
  fun archiveGoal(prisonNumber: String, archiveGoalDto: ArchiveGoalDto): Either<ArchiveGoalProblem, Goal> {
    val goalReference = archiveGoalDto.reference
    log.info { "Archiving Goal with reference [$goalReference] for prisoner [$prisonNumber] because [${archiveGoalDto.reason}] with description [${archiveGoalDto.reasonOther ?: ""}]" }

    return goalPersistenceAdapter.getGoal(prisonNumber, goalReference).toOption()
      .toEither { GoalToBeArchivedCouldNotBeFound(prisonNumber, goalReference) }
      .flatMap { validateGoalIsValidForArchiving(it, prisonNumber, goalReference) }
      .flatMap { validateArchiveDto(archiveGoalDto, prisonNumber, goalReference) }
      .flatMap {
        goalPersistenceAdapter.archiveGoal(prisonNumber, archiveGoalDto)?.right() ?: GoalToBeArchivedCouldNotBeFound(
          prisonNumber,
          goalReference,
        ).left()
      }
  }

  private fun validateArchiveDto(
    archiveGoalDto: ArchiveGoalDto,
    prisonNumber: String,
    goalReference: UUID,
  ) = if (archiveGoalDto.reason == ReasonToArchiveGoal.OTHER && archiveGoalDto.reasonOther.isNullOrEmpty()) {
    ArchiveReasonIsOtherButNoDescriptionProvided(prisonNumber, goalReference).left()
  } else {
    Unit.right()
  }

  private fun validateGoalIsValidForArchiving(
    it: Goal,
    prisonNumber: String,
    goalReference: UUID,
  ) = if (it.status != GoalStatus.ACTIVE) {
    TriedToArchiveAGoalInAnInvalidState(prisonNumber, goalReference, it.status).left()
  } else {
    it.right()
  }

  private fun actionPlanDoesNotExist(prisonNumber: String) =
    actionPlanPersistenceAdapter.getActionPlan(prisonNumber) == null

  private fun newActionPlan(prisonNumber: String, createGoalDtos: List<CreateGoalDto>) =
    CreateActionPlanDto(
      prisonNumber = prisonNumber,
      reviewDate = null,
      goals = createGoalDtos,
    )
}
