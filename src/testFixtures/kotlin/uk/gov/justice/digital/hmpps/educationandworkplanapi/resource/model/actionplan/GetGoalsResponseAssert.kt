package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetGoalsResponse
import java.util.*
import java.util.function.Consumer

fun assertThat(actual: GetGoalsResponse?) = GetGoalsResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [GetGoalsResponse]
 */
class GetGoalsResponseAssert(actual: GetGoalsResponse?) : AbstractObjectAssert<GetGoalsResponseAssert, GetGoalsResponse?>(actual, GetGoalsResponseAssert::class.java) {

  fun hasNumberOfGoals(expected: Int): GetGoalsResponseAssert {
    isNotNull
    with(actual!!) {
      if (goals.size != expected) {
        failWithMessage("Expected $expected goals but there were ${goals.size} goals")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [GoalResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [GoalResponseAssert].
   * Returns this [GetGoalsResponseAssert] to allow further chained assertions on the parent [GetGoalsResponse]
   *
   * The `goalNumber` parameter is not zero indexed to make for better readability in tests. IE. the first goal
   * should be referenced as `.goal(1) { .... }`
   */
  fun goal(goalNumber: Int, consumer: Consumer<GoalResponseAssert>): GetGoalsResponseAssert {
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
   * Returns this [GetGoalsResponseAssert] to allow further chained assertions on the parent [GetGoalsResponse]
   * The assertions on all [GoalResponse]s must pass as true.
   */
  fun allGoals(consumer: Consumer<GoalResponseAssert>): GetGoalsResponseAssert {
    isNotNull
    with(actual!!) {
      goals.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }
}
