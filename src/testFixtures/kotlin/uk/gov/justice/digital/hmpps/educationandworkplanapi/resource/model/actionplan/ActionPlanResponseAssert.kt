package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
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

  fun hasNumberOfGoals(numberOfGoals: Int): ActionPlanResponseAssert {
    isNotNull
    with(actual!!) {
      if (goals.size != numberOfGoals) {
        failWithMessage("Expected ActionPlan to be have $numberOfGoals goals set, but has ${goals.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [GoalResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [GoalResponseAssert].
   * Returns this [ActionPlanResponseAssert] to allow further chained assertions on the parent [ActionPlanResponse]
   *
   * The `goalNumber` parameter is not zero indexed to make for better readability in tests. IE. the first goal
   * should be referenced as `.goal(1) { .... }`
   */
  fun goal(goalNumber: Int, consumer: Consumer<GoalResponseAssert>): ActionPlanResponseAssert {
    isNotNull
    with(actual!!) {
      val goal = goals[goalNumber - 1]
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

  /**
   * Allows for assertion chaining into the specified child [GoalResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [GoalResponseAssert].
   * Returns this [ActionPlanResponseAssert] to allow further chained assertions on the parent [ActionPlanResponse]
   *
   * The `goalNumbers` parameter is not zero indexed to make for better readability in tests. IE. the first and third goals
   * should be referenced as `.anyOfGoalNumber(1, 3) { .... }`
   */
  fun anyOfGoalNumber(vararg goalNumbers: Int, consumer: Consumer<GoalResponseAssert>): ActionPlanResponseAssert {
    isNotNull
    with(actual!!) {
      val goals = goals.filterIndexed { index, _ -> goalNumbers.contains(index + 1) }
      val assertionPassed = goals.any {
        try {
          consumer.accept(assertThat(it))
          true
        } catch (e: AssertionError) {
          false
        }
      }
      if (!assertionPassed) {
        failWithMessage("Expected at least one goal to have passed assertions")
      }
    }
    return this
  }
}
