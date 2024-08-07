package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualification
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationLevel
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousQualificationsResponse
import java.time.OffsetDateTime
import java.util.UUID

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

  fun hasQualifications(expected: List<AchievedQualification>): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (qualifications != expected) {
        failWithMessage("Expected educationLevel to be $qualifications, but was $qualifications")
      }
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

  fun wasCreatedAfter(dateTime: OffsetDateTime): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (!createdAt.isAfter(dateTime)) {
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

  fun wasUpdatedAfter(dateTime: OffsetDateTime): PreviousQualificationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (!updatedAt.isAfter(dateTime)) {
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
