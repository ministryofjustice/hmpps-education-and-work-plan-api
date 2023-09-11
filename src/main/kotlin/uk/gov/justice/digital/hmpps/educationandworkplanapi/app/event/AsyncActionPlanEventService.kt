package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.event

import mu.KotlinLogging
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanEventService

private val log = KotlinLogging.logger {}

/**
 * Implementation of [ActionPlanEventService] for performing additional asynchronous actions related to [ActionPlan]
 * events.
 */
@Component
class AsyncActionPlanEventService() : ActionPlanEventService {
  override fun actionPlanCreated(actionPlan: ActionPlan) {
    log.info { "ActionPlan created event for prisoner [${actionPlan.prisonNumber}]" }
  }
}
