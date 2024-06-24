package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: InductionResponse?) = InductionResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [InductionResponse].
 */
class InductionResponseAssert(actual: InductionResponse?) :
  AbstractObjectAssert<InductionResponseAssert, InductionResponse?>(
    actual,
    InductionResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }
  fun wasCreatedAt(expected: OffsetDateTime): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAfter(dateTime: OffsetDateTime): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (!createdAt.isAfter(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAfter(dateTime: OffsetDateTime): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (!updatedAt.isAfter(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedByDisplayName(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [PreviousWorkExperiencesResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [PreviousWorkExperiencesResponseAssert].
   * Returns this [InductionResponseAssert] to allow further chained assertions on the parent [InductionResponse]
   */
  fun previousWorkExperiences(consumer: Consumer<PreviousWorkExperiencesResponseAssert>): InductionResponseAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(previousWorkExperiences))
    }
    return this
  }
}
