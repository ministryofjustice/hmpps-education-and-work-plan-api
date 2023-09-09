package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules for the Goal domain, and is how applications
 * must create and manage [Goal]s.
 *
 * Applications using [Goal]s must new up an instance of this class providing an implementation of
 * [GoalPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain. Service method behaviour can however be customized and extended by creating an instance of this class with a
 * [GoalServiceDecorator]. If not specified, the default implementation is a [NoopGoalServiceDecorator].
 *
 */
class GoalService(
  private val persistenceAdapter: GoalPersistenceAdapter,
  private val goalServiceDecorator: GoalServiceDecorator = NoopGoalServiceDecorator(),
) {

  /**
   * Creates a new [Goal] for the prisoner identified by their prison number, with the data in the specified [CreateGoalDto]
   */
  fun createGoal(prisonNumber: String, createGoalDto: CreateGoalDto): Goal {
    goalServiceDecorator.beforeCreateGoal(prisonNumber, createGoalDto)
    log.info { "Creating new Goal for prisoner [$prisonNumber]" }
    return persistenceAdapter.createGoal(prisonNumber, createGoalDto)
      .also {
        goalServiceDecorator.afterCreateGoal(prisonNumber, createGoalDto, it)
      }
  }

  /**
   * Returns a [Goal] identified by its `prisonNumber` and `goalReference`.
   * Throws [GoalNotFoundException] if the [Goal] cannot be found.
   */
  fun getGoal(prisonNumber: String, goalReference: UUID): Goal {
    goalServiceDecorator.beforeGetGoal(prisonNumber, goalReference)
    log.info { "Retrieving Goal with reference [$goalReference] for prisoner [$prisonNumber]" }
    return persistenceAdapter.getGoal(prisonNumber, goalReference)
      ?.also {
        goalServiceDecorator.afterGetGoal(prisonNumber, goalReference, it)
      }
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      }
  }

  /**
   * Updates a [Goal], identified by its `prisonNumber` and `goalReference`, from the specified [UpdateGoalDto].
   * Throws [GoalNotFoundException] if the [Goal] to be updated cannot be found.
   */
  fun updateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto): Goal {
    goalServiceDecorator.beforeUpdateGoal(prisonNumber, updatedGoalDto)
    val goalReference = updatedGoalDto.reference
    log.info { "Updating Goal with reference [$goalReference] for prisoner [$prisonNumber]" }
    return persistenceAdapter.updateGoal(prisonNumber, updatedGoalDto)
      ?.also {
        goalServiceDecorator.afterUpdateGoal(prisonNumber, updatedGoalDto, it)
      }
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      }
  }
}
