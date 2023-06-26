package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert

fun assertThat(actual: ActionPlanResponse?) = ActionPlanResponseAssert(actual)

/**
 * AssertJ custom assertion for [ActionPlanResponse]
 */
class ActionPlanResponseAssert(actual: ActionPlanResponse?) :
  AbstractObjectAssert<ActionPlanResponseAssert, ActionPlanResponse?>(actual, ActionPlanResponseAssert::class.java) {

  fun isForPrisonNumber(expected: String): ActionPlanResponseAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun hasNoGoalsSet(): ActionPlanResponseAssert {
    isNotNull
    with(actual!!) {
      if (goals.isNotEmpty()) {
        failWithMessage("Expected ActionPlan to be have no goals set, but has $goals")
      }
    }
    return this
  }
}
