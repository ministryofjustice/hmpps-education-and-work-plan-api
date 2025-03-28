package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanSummary
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateActionPlanDto

private val log = KotlinLogging.logger {}

/**
 * Service class exposing methods that implement the business rules for a Prisoner's [ActionPlan].
 *
 * Applications using [ActionPlan]s must new up an instance of this class providing an implementation of
 * [ActionPlanPersistenceAdapter].
 *
 * This class is deliberately final so that it cannot be subclassed, ensuring that the business rules stay within the
 * domain.
 */
class ActionPlanService(
  private val persistenceAdapter: ActionPlanPersistenceAdapter,
  private val actionPlanEventService: ActionPlanEventService,
  private val goalNotesService: GoalNotesService,
) {

  /**
   * Creates an [ActionPlan] for a prisoner, containing at least one or more Goals.
   */
  fun createActionPlan(createActionPlanDto: CreateActionPlanDto): ActionPlan {
    with(createActionPlanDto) {
      log.info { "Creating ActionPlan for prisoner [$prisonNumber]" }

      if (persistenceAdapter.getActionPlan(prisonNumber) != null) {
        throw ActionPlanAlreadyExistsException(prisonNumber)
      }

      return persistenceAdapter.createActionPlan(createActionPlanDto)
        .also { goalNotesService.createNotes(prisonNumber, it.goals) }
        .also { actionPlanEventService.actionPlanCreated(it) }
    }
  }

  /**
   * Retrieves a Prisoner's [ActionPlan] based on their prison number.
   * Throws [ActionPlanNotFoundException] if the [ActionPlan] cannot be found.
   */
  fun getActionPlan(prisonNumber: String): ActionPlan {
    log.debug { "Retrieving Action Plan for prisoner [$prisonNumber]" }
    return persistenceAdapter.getActionPlan(prisonNumber)
      ?.apply {
        goals.onEach { it.notes = goalNotesService.getNotes(it.reference) }
      }
      ?: throw ActionPlanNotFoundException(prisonNumber)
  }

  fun getActionPlanSummaries(prisonNumbers: List<String>): List<ActionPlanSummary> {
    log.debug { "Retrieving Action Plan Summaries for ${prisonNumbers.size} prisoners" }
    return if (prisonNumbers.isNotEmpty()) persistenceAdapter.getActionPlanSummaries(prisonNumbers) else emptyList()
  }
}
