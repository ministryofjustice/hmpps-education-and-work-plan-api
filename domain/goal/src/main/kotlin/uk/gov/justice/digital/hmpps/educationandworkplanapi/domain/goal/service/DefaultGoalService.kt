package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.Goal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.UpdateGoalDto
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * Default implementation of [GoalService].
 *
 * Applications using [Goal]s must new up an instance of this class providing an implementation of
 * [GoalPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain. Service method behaviour can however be customized and extended by creating an implementation of [GoalService]
 * that wraps and delegates to an instance of [DefaultGoalService].
 */
class DefaultGoalService(
  private val persistenceAdapter: GoalPersistenceAdapter,
) : GoalService {

  /**
   * Creates a new [Goal] for the prisoner identified by their prison number, with the data in the specified [CreateGoalDto]
   */
  override fun createGoal(prisonNumber: String, createGoalDto: CreateGoalDto): Goal {
    log.info { "Creating new Goal for prisoner [$prisonNumber]" }
    return persistenceAdapter.createGoal(prisonNumber, createGoalDto)
  }

  /**
   * Returns a [Goal] identified by its `prisonNumber` and `goalReference`.
   * Throws [GoalNotFoundException] if the [Goal] cannot be found.
   */
  override fun getGoal(prisonNumber: String, goalReference: UUID): Goal {
    log.info { "Retrieving Goal with reference [$goalReference] for prisoner [$prisonNumber]" }
    return persistenceAdapter.getGoal(prisonNumber, goalReference)
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      }
  }

  /**
   * Updates a [Goal], identified by its `prisonNumber` and `goalReference`, from the specified [UpdateGoalDto].
   * Throws [GoalNotFoundException] if the [Goal] to be updated cannot be found.
   */
  override fun updateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto): Goal {
    val goalReference = updatedGoalDto.reference
    log.info { "Updating Goal with reference [$goalReference] for prisoner [$prisonNumber]" }
    return persistenceAdapter.updateGoal(prisonNumber, updatedGoalDto)
      ?: throw GoalNotFoundException(prisonNumber, goalReference).also {
        log.info { "Goal with reference [$goalReference] for prisoner [$prisonNumber] not found" }
      }
  }
}
