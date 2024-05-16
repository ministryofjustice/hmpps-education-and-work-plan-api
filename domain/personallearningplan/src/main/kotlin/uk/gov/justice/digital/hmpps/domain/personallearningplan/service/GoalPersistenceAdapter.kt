package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import java.util.UUID

/**
 * Persistence Adapter for [Goal] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo, Redis etc
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by
 * [GoalService].
 */
interface GoalPersistenceAdapter {

  /**
   * Creates new [Goal]s for the prisoner identified by their prison number.
   */
  fun createGoals(prisonNumber: String, createGoalDtos: List<CreateGoalDto>): List<Goal>

  /**
   * Returns a [Goal] identified by its `prisonNumber` and `goalReference` if found, otherwise `null`.
   */
  fun getGoal(prisonNumber: String, goalReference: UUID): Goal?

  /**
   * Updates a [Goal] identified by its `prisonNumber` and [UpdateGoalDto.reference]
   */
  fun updateGoal(prisonNumber: String, updatedGoalDto: UpdateGoalDto): Goal?
}
