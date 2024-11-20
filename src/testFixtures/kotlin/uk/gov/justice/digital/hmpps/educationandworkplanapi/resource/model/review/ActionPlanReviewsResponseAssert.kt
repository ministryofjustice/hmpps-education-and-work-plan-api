package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompletedActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ScheduledActionPlanReviewResponse
import java.util.function.Consumer

fun assertThat(actual: ActionPlanReviewsResponse?) = ActionPlanReviewsResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [ActionPlanReviewsResponse].
 */
class ActionPlanReviewsResponseAssert(actual: ActionPlanReviewsResponse?) :
  AbstractObjectAssert<ActionPlanReviewsResponseAssert, ActionPlanReviewsResponse?>(
    actual,
    ActionPlanReviewsResponseAssert::class.java,
  ) {

  fun hasNumberOfCompletedReviews(expected: Int): ActionPlanReviewsResponseAssert {
    isNotNull
    with(actual!!) {
      if (completedReviews.size != expected) {
        failWithMessage("Expected Action Plan Reviews Response to have $expected Completed Reviews, but was ${completedReviews.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [CompletedActionPlanReviewResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [CompletedActionPlanReviewResponseAssert].
   * Returns this [ActionPlanReviewsResponseAssert] to allow further chained assertions on the parent [ActionPlanReviewsResponse]
   *
   * The `completedReviewNumber` parameter is not zero indexed to make for better readability in tests. IE. the first completed review
   * should be referenced as `.completedReviewNumber(1) { .... }`
   */
  fun completedReview(completedReviewNumber: Int, consumer: Consumer<CompletedActionPlanReviewResponseAssert>): ActionPlanReviewsResponseAssert {
    isNotNull
    with(actual!!) {
      val completedReview = completedReviews[completedReviewNumber - 1]
      consumer.accept(assertThat(completedReview))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [CompletedActionPlanReviewResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [CompletedActionPlanReviewResponseAssert].
   * Returns this [ActionPlanReviewsResponseAssert] to allow further chained assertions on the parent [ActionPlanReviewsResponse]
   * The assertions on all [CompletedActionPlanReviewResponse]s must pass as true.
   */
  fun allCompletedReviews(consumer: Consumer<CompletedActionPlanReviewResponseAssert>): ActionPlanReviewsResponseAssert {
    isNotNull
    with(actual!!) {
      completedReviews.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [ScheduledActionPlanReviewResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [ScheduledActionPlanReviewResponseAssert].
   * Returns this [ActionPlanReviewsResponseAssert] to allow further chained assertions on the parent [ActionPlanReviewsResponse]
   */
  fun latestReviewSchedule(consumer: Consumer<ScheduledActionPlanReviewResponseAssert>): ActionPlanReviewsResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(latestReviewSchedule))
    }
    return this
  }
}
