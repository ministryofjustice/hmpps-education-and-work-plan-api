package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.sar

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompletedActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SarGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.EducationResponseAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.InductionResponseAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.InductionScheduleResponseAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.CompletedActionPlanReviewResponseAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review.assertThat
import java.util.function.Consumer

fun assertThat(actual: SubjectAccessRequestContent?) = SubjectAccessRequestContentAssert(actual)

/**
 * AssertJ custom assertion for a single [SubjectAccessRequestContent]
 */
class SubjectAccessRequestContentAssert(actual: SubjectAccessRequestContent?) : AbstractObjectAssert<SubjectAccessRequestContentAssert, SubjectAccessRequestContent?>(actual, SubjectAccessRequestContentAssert::class.java) {

  fun hasNoInduction(): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (induction != null) {
        failWithMessage("Expected induction to be null but was: $induction")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [InductionResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [InductionResponseAssert].
   * Returns this [SubjectAccessRequestContentAssert] to allow further chained assertions on the parent [SubjectAccessRequestContent]
   */
  fun induction(consumer: Consumer<InductionResponseAssert>): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(induction))
    }
    return this
  }

  fun hasNoGoals(): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (!goals.isNullOrEmpty()) {
        failWithMessage("Expected no goals but has ${goals.size} goals")
      }
    }
    return this
  }

  fun hasNumberOfGoals(expected: Int): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (goals?.size != expected) {
        failWithMessage("Expected SubjectAccessRequestContent to be have $expected goals set, but has ${goals?.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [SarGoalResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [SarGoalResponseAssert].
   * Returns this [SubjectAccessRequestContentAssert] to allow further chained assertions on the parent [SubjectAccessRequestContent]
   *
   * The `goalNumber` parameter is not zero indexed to make for better readability in tests. IE. the first goal
   * should be referenced as `.goal(1) { .... }`
   */
  fun goal(goalNumber: Int, consumer: Consumer<SarGoalResponseAssert>): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      val goal = goals?.get(goalNumber - 1)
      consumer.accept(assertThat(goal))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [SarGoalResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [SarGoalResponseAssert].
   * Returns this [SubjectAccessRequestContentAssert] to allow further chained assertions on the parent [SubjectAccessRequestContent]
   * The assertions on all [SarGoalResponse]s must pass as true.
   */
  fun allGoals(consumer: Consumer<SarGoalResponseAssert>): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      goals?.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  fun hasNoEducation(): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (education != null) {
        failWithMessage("Expected education to be null but was: $education")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [EducationResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [EducationResponseAssert].
   * Returns this [SubjectAccessRequestContentAssert] to allow further chained assertions on the parent [SubjectAccessRequestContent]
   */
  fun education(consumer: Consumer<EducationResponseAssert>): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(education))
    }
    return this
  }

  fun hasNoCompletedReviews(): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (!completedReviews.isNullOrEmpty()) {
        failWithMessage("Expected no completed reviews but has ${completedReviews.size} completed reviews")
      }
    }
    return this
  }

  fun hasNumberOfCompletedReviews(expected: Int): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (completedReviews?.size != expected) {
        failWithMessage("Expected SubjectAccessRequestContent to be have $expected completed reviews set, but has ${completedReviews?.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [CompletedActionPlanReviewResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [CompletedActionPlanReviewResponseAssert].
   * Returns this [SubjectAccessRequestContentAssert] to allow further chained assertions on the parent [SubjectAccessRequestContent]
   *
   * The `completedReviewNumber` parameter is not zero indexed to make for better readability in tests. IE. the first completed review
   * should be referenced as `.completedReview(1) { .... }`
   */
  fun completedReview(completedReviewNumber: Int, consumer: Consumer<CompletedActionPlanReviewResponseAssert>): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      val goal = completedReviews?.get(completedReviewNumber - 1)
      consumer.accept(assertThat(goal))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [CompletedActionPlanReviewResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [CompletedActionPlanReviewResponseAssert].
   * Returns this [SubjectAccessRequestContentAssert] to allow further chained assertions on the parent [SubjectAccessRequestContent]
   * The assertions on all [CompletedActionPlanReviewResponse]s must pass as true.
   */
  fun allCompletedReviews(consumer: Consumer<CompletedActionPlanReviewResponseAssert>): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      completedReviews?.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  fun hasNoInductionScheduleRecords(): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (!inductionScheduleHistory.isNullOrEmpty()) {
        failWithMessage("Expected no induction schedule records but has ${inductionScheduleHistory.size} goals")
      }
    }
    return this
  }

  fun hasNumberOfInductionScheduleRecords(expected: Int): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (inductionScheduleHistory?.size != expected) {
        failWithMessage("Expected SubjectAccessRequestContent to be have $expected induction schedule records, but has ${inductionScheduleHistory?.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [InductionScheduleResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [InductionScheduleResponseAssert].
   * Returns this [SubjectAccessRequestContentAssert] to allow further chained assertions on the parent [SubjectAccessRequestContent]
   *
   * The `inductionScheduleNumber` parameter is not zero indexed to make for better readability in tests. IE. the first induction schedule
   * should be referenced as `.inductionSchedule(1) { .... }`
   */
  fun inductionScheduleRecord(inductionScheduleNumber: Int, consumer: Consumer<InductionScheduleResponseAssert>): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      val goal = inductionScheduleHistory?.get(inductionScheduleNumber - 1)
      consumer.accept(assertThat(goal))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [InductionScheduleResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [InductionScheduleResponseAssert].
   * Returns this [SubjectAccessRequestContentAssert] to allow further chained assertions on the parent [SubjectAccessRequestContent]
   * The assertions on all [InductionScheduleResponseAssert]s must pass as true.
   */
  fun allInductionScheduleRecords(consumer: Consumer<InductionScheduleResponseAssert>): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      inductionScheduleHistory?.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  fun hasNoReviewScheduleRecords(): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (!reviewScheduleHistory.isNullOrEmpty()) {
        failWithMessage("Expected no review schedule records but has ${reviewScheduleHistory.size} goals")
      }
    }
    return this
  }

  fun hasNumberOfReviewScheduleRecords(expected: Int): SubjectAccessRequestContentAssert {
    isNotNull
    with(actual!!) {
      if (reviewScheduleHistory?.size != expected) {
        failWithMessage("Expected SubjectAccessRequestContent to be have $expected review schedule records, but has ${reviewScheduleHistory?.size}")
      }
    }
    return this
  }
}
