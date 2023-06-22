package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlan

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
   * Returns an [ActionPlan] if found, otherwise `null`.
   */
  fun getActionPlan(prisonNumber: String): ActionPlan?
}
