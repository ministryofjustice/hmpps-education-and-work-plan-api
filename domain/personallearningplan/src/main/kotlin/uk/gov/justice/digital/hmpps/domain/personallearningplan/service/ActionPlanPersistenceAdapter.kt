package uk.gov.justice.digital.hmpps.domain.personallearningplan.service

import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateActionPlanDto

/**
 * Persistence Adapter for [ActionPlan] instances.
 *
 * Implementations should use the underlying persistence of the application in question, eg: JPA, Mongo, Dynamo, Redis etc
 *
 * Implementations should not throw exceptions. These are not part of the interface and are not checked or handled by
 * [ActionPlanService].
 */
interface ActionPlanPersistenceAdapter {

  /**
   * Creates a new [ActionPlan] and returns persisted instance.
   */
  fun createActionPlan(createActionPlanDto: CreateActionPlanDto): ActionPlan

  /**
   * Returns an [ActionPlan] if found, otherwise `null`.
   */
  fun getActionPlan(prisonNumber: String): ActionPlan?
}
