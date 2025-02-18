package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousQualificationsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.AchievedQualificationResponseAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education.assertThat
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: PreviousQualificationsResponse?) = PreviousQualificationsResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [PreviousQualificationsResponse].
 */
class PreviousQualificationsResponseAssert(actual: PreviousQualificationsResponse?) :
  AbstractObjectAssert<PreviousQualificationsResponseAssert, PreviousQualificationsResponse?>(
    actual,
    PreviousQualificationsResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun hasEducationLevel(expected: EducationLevel): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (educationLevel != expected) {
        failWithMessage("Expected educationLevel to be $expected, but was $educationLevel")
      }
    }
    return this
  }

  fun hasQualifications(expected: List<AchievedQualificationResponse>): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (qualifications != expected) {
        failWithMessage("Expected educationLevel to be $expected, but was $qualifications")
      }
    }
    return this
  }

  fun hasNumberOfQualifications(expected: Int): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (qualifications.size != expected) {
        failWithMessage("Expected educationLevel to have $expected qualifications, but has ${qualifications.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [AchievedQualificationResponseAssert]. Takes a lambda as the method argument
   * to call assertion methods provided by [AchievedQualificationResponseAssert].
   * Returns this [PreviousQualificationsResponseAssert] to allow further chained assertions on the parent [GoalResponse]
   *
   * The `qualificationNumber` parameter is not zero indexed to make for better readability in tests. IE. the first qualification
   * should be referenced as `.qualification(1) { .... }`
   */
  fun qualification(
    qualificationNumber: Int,
    consumer: Consumer<AchievedQualificationResponseAssert>,
  ): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      val qualification = qualifications[qualificationNumber - 1]
      consumer.accept(assertThat(qualification))
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: OffsetDateTime): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedByDisplayName(expected: String): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }
}
