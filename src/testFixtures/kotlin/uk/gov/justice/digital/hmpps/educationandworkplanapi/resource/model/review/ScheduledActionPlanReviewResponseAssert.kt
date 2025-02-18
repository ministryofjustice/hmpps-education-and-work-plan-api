package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleCalculationRule
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ScheduledActionPlanReviewResponse
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun assertThat(actual: ScheduledActionPlanReviewResponse?) = ScheduledActionPlanReviewResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [ScheduledActionPlanReviewResponse].
 */
class ScheduledActionPlanReviewResponseAssert(actual: ScheduledActionPlanReviewResponse?) :
  AbstractObjectAssert<ScheduledActionPlanReviewResponseAssert, ScheduledActionPlanReviewResponse?>(
    actual,
    ScheduledActionPlanReviewResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: OffsetDateTime): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedByDisplayName(expected: String): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  fun hasReviewDateFrom(expected: LocalDate): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (reviewDateFrom != expected) {
        failWithMessage("Expected reviewDateFrom to be $expected, but was $reviewDateFrom")
      }
    }
    return this
  }

  fun hasReviewDateTo(expected: LocalDate): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (reviewDateTo != expected) {
        failWithMessage("Expected reviewDateTo to be $expected, but was $reviewDateTo")
      }
    }
    return this
  }

  fun hasCalculationRule(expected: ReviewScheduleCalculationRule): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (calculationRule != expected) {
        failWithMessage("Expected calculationRule to be $expected, but was $calculationRule")
      }
    }
    return this
  }

  fun hasStatus(expected: ReviewScheduleStatus): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (status != expected) {
        failWithMessage("Expected status to be $expected, but was $status")
      }
    }
    return this
  }

  fun isVersion(expected: Int): ScheduledActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (version != expected) {
        failWithMessage("Expected version to be $expected, but was $version")
      }
    }
    return this
  }
}
