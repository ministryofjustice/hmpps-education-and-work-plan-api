package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationResponse
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: EducationResponse?) = EducationResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [EducationResponse].
 */
class EducationResponseAssert(actual: EducationResponse?) :
  AbstractObjectAssert<EducationResponseAssert, EducationResponse?>(
    actual,
    EducationResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun hasEducationLevel(expected: EducationLevel): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (educationLevel != expected) {
        failWithMessage("Expected educationLevel to be $expected, but was $educationLevel")
      }
    }
    return this
  }

  fun hasQualifications(expected: List<AchievedQualificationResponse>): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (qualifications != expected) {
        failWithMessage("Expected qualifications to be $expected, but was $qualifications")
      }
    }
    return this
  }

  fun hasNumberOfQualifications(expected: Int): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (qualifications.size != expected) {
        failWithMessage("Expected number of qualifications to be $expected, but was ${qualifications.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [AchievedQualificationResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [AchievedQualificationResponseAssert].
   * Returns this [EducationResponseAssert] to allow further chained assertions on the parent [EducationResponse]
   * The assertions on all [AchievedQualificationResponse]s must pass as true.
   */
  fun allQualifications(consumer: Consumer<AchievedQualificationResponseAssert>): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      qualifications.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [AchievedQualificationResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [AchievedQualificationResponseAssert].
   * Returns this [EducationResponseAssert] to allow further chained assertions on the parent [EducationResponse]
   *
   * The `subject` parameter is to find the specific qualification to assert by its subject name
   */
  fun qualificationBySubject(subject: String, consumer: Consumer<AchievedQualificationResponseAssert>): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      val qualification = this.qualifications.find { it.subject == subject }
      consumer.accept(assertThat(qualification))
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBefore(dateTime)) {
        failWithMessage("Expected createdAt to be at or after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: OffsetDateTime): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt.isBefore(dateTime)) {
        failWithMessage("Expected updatedAt to be at or after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedByDisplayName(expected: String): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): EducationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }
}
