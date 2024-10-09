package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CompleteGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UpdateGoalDto
import java.util.*

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

  /**
   * Archives a [Goal] identified by its `prisonNumber` and [ArchiveGoalDTO.reference]
   */
  fun archiveGoal(prisonNumber: String, archiveGoalDto: ArchiveGoalDto): Goal?

  /**
   * Unarchives a [Goal] identified by its `prisonNumber` and [UnarchiveGoalDTO.reference]
   */
  fun unarchiveGoal(prisonNumber: String, unarchiveGoalDto: UnarchiveGoalDto): Goal?

  /**
   * Archives a [Goal] identified by its `prisonNumber` and [ArchiveGoalDTO.reference]
   */
  fun completeGoal(prisonNumber: String, completeGoalDto: CompleteGoalDto): Goal?

  /**
   * Gets all [Goal]s for a prisoner identified by `prisonNumber` or returns null if the prisoner has no action plan
   */
  fun getGoals(prisonNumber: String): List<Goal>?
}
