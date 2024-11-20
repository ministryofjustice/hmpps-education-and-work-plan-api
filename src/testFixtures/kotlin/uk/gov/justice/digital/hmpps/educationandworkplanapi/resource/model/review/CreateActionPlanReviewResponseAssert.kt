package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ScheduledActionPlanReviewResponse
import java.util.function.Consumer

fun assertThat(actual: CreateActionPlanReviewResponse?) = CreateActionPlanReviewResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [CreateActionPlanReviewResponse].
 */
class CreateActionPlanReviewResponseAssert(actual: CreateActionPlanReviewResponse?) :
  AbstractObjectAssert<CreateActionPlanReviewResponseAssert, CreateActionPlanReviewResponse?>(
    actual,
    CreateActionPlanReviewResponseAssert::class.java,
  ) {

  fun wasLastReviewBeforeRelease(): CreateActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (!wasLastReviewBeforeRelease) {
        failWithMessage("Expected wasLastReviewBeforeRelease to be true, but was false")
      }
    }
    return this
  }

  fun wasNotLastReviewBeforeRelease(): CreateActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (wasLastReviewBeforeRelease) {
        failWithMessage("Expected wasLastReviewBeforeRelease to be false, but was true")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [ScheduledActionPlanReviewResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [ScheduledActionPlanReviewResponseAssert].
   * Returns this [CreateActionPlanReviewResponseAssert] to allow further chained assertions on the parent [CreateActionPlanReviewResponse]
   */
  fun latestReviewSchedule(consumer: Consumer<ScheduledActionPlanReviewResponseAssert>): CreateActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(
        assertThat(latestReviewSchedule),
      )
    }
    return this
  }
}
