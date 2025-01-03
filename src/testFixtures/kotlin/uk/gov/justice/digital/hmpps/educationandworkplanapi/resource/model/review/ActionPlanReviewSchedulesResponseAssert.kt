package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanReviewSchedulesResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ScheduledActionPlanReviewResponse
import java.util.function.Consumer

fun assertThat(actual: ActionPlanReviewSchedulesResponse?) = ActionPlanReviewSchedulesResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [ActionPlanReviewSchedulesResponse].
 */
class ActionPlanReviewSchedulesResponseAssert(actual: ActionPlanReviewSchedulesResponse?) :
  AbstractObjectAssert<ActionPlanReviewSchedulesResponseAssert, ActionPlanReviewSchedulesResponse?>(
    actual,
    ActionPlanReviewSchedulesResponseAssert::class.java,
  ) {

  fun hasNumberOfReviewSchedules(expected: Int): ActionPlanReviewSchedulesResponseAssert {
    isNotNull
    with(actual!!) {
      if (reviewSchedules.size != expected) {
        failWithMessage("Expected Action Plan Review Schedules Response to have $expected Review Schedules, but was ${reviewSchedules.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [ScheduledActionPlanReviewResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [ScheduledActionPlanReviewResponseAssert].
   * Returns this [ActionPlanReviewSchedulesResponseAssert] to allow further chained assertions on the parent [ActionPlanReviewSchedulesResponse]
   *
   * The `reviewScheduleVersionNumber` parameter is the version number of the [ScheduledActionPlanReviewResponse] to make assertions against.
   */
  fun reviewScheduleAtVersion(reviewScheduleVersionNumber: Int, consumer: Consumer<ScheduledActionPlanReviewResponseAssert>): ActionPlanReviewSchedulesResponseAssert {
    isNotNull
    with(actual!!) {
      val reviewSchedule = reviewSchedules.find { it.version == reviewScheduleVersionNumber }
      consumer.accept(assertThat(reviewSchedule))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [ScheduledActionPlanReviewResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [ScheduledActionPlanReviewResponseAssert].
   * Returns this [ActionPlanReviewSchedulesResponseAssert] to allow further chained assertions on the parent [ActionPlanReviewSchedulesResponse]
   * The assertions on all [ScheduledActionPlanReviewResponse]s must pass as true.
   */
  fun allReviewSchedules(consumer: Consumer<ScheduledActionPlanReviewResponseAssert>): ActionPlanReviewSchedulesResponseAssert {
    isNotNull
    with(actual!!) {
      reviewSchedules.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }
}
