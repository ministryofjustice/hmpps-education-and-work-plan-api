package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.education

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.AchievedQualificationResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.QualificationLevel
import java.time.OffsetDateTime
import java.util.UUID

fun assertThat(actual: AchievedQualificationResponse?) = AchievedQualificationResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [AchievedQualificationResponse].
 */
class AchievedQualificationResponseAssert(actual: AchievedQualificationResponse?) :
  AbstractObjectAssert<AchievedQualificationResponseAssert, AchievedQualificationResponse?>(
    actual,
    AchievedQualificationResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun hasSubject(expected: String): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (subject != expected) {
        failWithMessage("Expected subject to be $expected, but was $subject")
      }
    }
    return this
  }

  fun hasLevel(expected: QualificationLevel): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (level != expected) {
        failWithMessage("Expected level to be $expected, but was $level")
      }
    }
    return this
  }

  fun hasGrade(expected: String): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (grade != expected) {
        failWithMessage("Expected level to be $expected, but was $grade")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected createdAt to be at or after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: OffsetDateTime): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be at or after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): AchievedQualificationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }
}
