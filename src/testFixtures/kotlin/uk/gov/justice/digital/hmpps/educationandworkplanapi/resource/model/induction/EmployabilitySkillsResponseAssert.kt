package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillRating
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillSessionType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EmployabilitySkillType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetEmployabilitySkillsResponse
import java.time.OffsetDateTime

fun assertThat(actual: GetEmployabilitySkillsResponse?) = EmployabilitySkillAssert(actual)

/**
 * AssertJ custom assertion for a single [GetEmployabilitySkillsResponse].
 */
class EmployabilitySkillAssert(actual: GetEmployabilitySkillsResponse?) :
  AbstractObjectAssert<EmployabilitySkillAssert, GetEmployabilitySkillsResponse?>(
    actual,
    EmployabilitySkillAssert::class.java,
  ) {

  fun wasCreatedAt(expected: OffsetDateTime): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (!createdAt.isEqual(expected)) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (!updatedAt.isEqual(expected)) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: OffsetDateTime): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedByDisplayName(expected: String): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  fun hasSkillType(expected: EmployabilitySkillType): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (employabilitySkillType != expected) {
        failWithMessage("Expected employabilitySkillType to be $expected, but was $employabilitySkillType")
      }
    }
    return this
  }

  fun hasSkillRating(expected: EmployabilitySkillRating): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (employabilitySkillRating != expected) {
        failWithMessage("Expected employabilitySkillRating to be $expected, but was $employabilitySkillRating")
      }
    }
    return this
  }

  fun hasEvidence(expected: String): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (evidence != expected) {
        failWithMessage("Expected evidence to be $expected, but was $evidence")
      }
    }
    return this
  }

  fun hasNoSessionType(): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (sessionType != null) {
        failWithMessage("Expected sessionType to be null, but was $sessionType")
      }
    }
    return this
  }

  fun hasSessionType(expected: EmployabilitySkillSessionType): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (sessionType != expected) {
        failWithMessage("Expected sessionType to be $expected, but was $sessionType")
      }
    }
    return this
  }

  fun hasNoSessionTypeDescription(): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (sessionTypeDescription != null) {
        failWithMessage("Expected sessionTypeDescription to be null, but was $sessionTypeDescription")
      }
    }
    return this
  }

  fun hasSessionTypeDescription(expected: String): EmployabilitySkillAssert {
    isNotNull
    with(actual!!) {
      if (sessionTypeDescription != expected) {
        failWithMessage("Expected sessionTypeDescription to be $expected, but was $sessionTypeDescription")
      }
    }
    return this
  }
}
