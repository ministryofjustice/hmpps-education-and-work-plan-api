package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert
import java.util.function.Consumer

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

  /**
   * Allows for assertion chaining into the specified child [GoalResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [GoalResponseAssert].
   * Returns this [ActionPlanResponseAssert] to allow further chained assertions on the parent [ActionPlanResponse]
   */
  fun goal(goalNumber: Int, consumer: Consumer<GoalResponseAssert>): ActionPlanResponseAssert {
    isNotNull
    with(actual!!) {
      val goal = goals[goalNumber]
      consumer.accept(assertThat(goal))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [GoalResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [GoalResponseAssert].
   * Returns this [ActionPlanResponseAssert] to allow further chained assertions on the parent [ActionPlanResponse]
   * The assertions on all [GoalResponse]s must pass as true.
   */
  fun allGoals(consumer: Consumer<GoalResponseAssert>): ActionPlanResponseAssert {
    isNotNull
    with(actual!!) {
      goals.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }
}
