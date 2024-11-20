package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.review

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompletedActionPlanReviewResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note.NoteResponseAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note.assertThat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: CompletedActionPlanReviewResponse?) = CompletedActionPlanReviewResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [CompletedActionPlanReviewResponse].
 */
class CompletedActionPlanReviewResponseAssert(actual: CompletedActionPlanReviewResponse?) :
  AbstractObjectAssert<CompletedActionPlanReviewResponseAssert, CompletedActionPlanReviewResponse?>(
    actual,
    CompletedActionPlanReviewResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAfter(dateTime: OffsetDateTime): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (!createdAt.isAfter(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasCompletedOn(expected: LocalDate): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (completedDate != expected) {
        failWithMessage("Expected completedDate to be $expected, but was $completedDate")
      }
    }
    return this
  }

  fun hadDeadlineDateOf(expected: LocalDate): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (deadlineDate != expected) {
        failWithMessage("Expected deadlineDate to be $expected, but was $deadlineDate")
      }
    }
    return this
  }

  fun wasConductedByTheUserWhoKeyedTheReviewIntoTheSystem(): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (conductedBy != null || conductedByRole != null) {
        failWithMessage("Expected both conductedBy and conductedByRole to be null, but there were $conductedBy and $conductedByRole")
      }
    }
    return this
  }

  fun wasConductedBy(expected: String): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (conductedBy != expected) {
        failWithMessage("Expected conductedBy to be $expected, but was $conductedBy")
      }
    }
    return this
  }

  fun wasConductedByRole(expected: String): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      if (conductedByRole != expected) {
        failWithMessage("Expected conductedByRole to be $expected, but was $conductedByRole")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [NoteResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [NoteResponseAssert].
   * Returns this [CompletedActionPlanReviewResponseAssert] to allow further chained assertions on the parent [CompletedActionPlanReviewResponse]
   */
  fun note(consumer: Consumer<NoteResponseAssert>): CompletedActionPlanReviewResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(note))
    }
    return this
  }
}
