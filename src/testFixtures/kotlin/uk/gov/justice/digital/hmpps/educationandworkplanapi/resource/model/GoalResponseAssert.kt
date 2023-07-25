package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert
import java.time.LocalDate
import java.time.OffsetDateTime

fun assertThat(actual: GoalResponse?) = GoalResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [GoalResponse]
 */
class GoalResponseAssert(actual: GoalResponse?) :
  AbstractObjectAssert<GoalResponseAssert, GoalResponse?>(actual, GoalResponseAssert::class.java) {

  fun wasCreatedBy(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun hasCreatedByDisplayName(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun hasUpdatedByDisplayName(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun hasTitle(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (title != expected) {
        failWithMessage("Expected title to be $expected, but was $title")
      }
    }
    return this
  }

  fun hasReviewDate(expected: LocalDate): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (reviewDate != expected) {
        failWithMessage("Expected reviewDate to be $expected, but was $reviewDate")
      }
    }
    return this
  }

  fun hasNumberOfSteps(expected: Int): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (steps.size != expected) {
        failWithMessage("Expected goal to have $expected Steps, but was ${steps.size}")
      }
    }
    return this
  }

  fun hasReference(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (goalReference != expected) {
        failWithMessage("Expected reference to be $expected, but was $goalReference")
      }
    }
    return this
  }
}
