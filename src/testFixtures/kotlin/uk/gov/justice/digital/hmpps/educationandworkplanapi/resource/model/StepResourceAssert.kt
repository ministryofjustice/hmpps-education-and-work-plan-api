package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert
import java.util.UUID

fun assertThat(actual: StepResponse?) = StepResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [StepResponse]
 */
class StepResponseAssert(actual: StepResponse?) :
  AbstractObjectAssert<StepResponseAssert, StepResponse?>(actual, StepResponseAssert::class.java) {

  fun hasTitle(expected: String): StepResponseAssert {
    isNotNull
    with(actual!!) {
      if (title != expected) {
        failWithMessage("Expected title to be $expected, but was $title")
      }
    }
    return this
  }

  fun hasTargetDateRange(expected: TargetDateRange): StepResponseAssert {
    isNotNull
    with(actual!!) {
      if (targetDateRange != expected) {
        failWithMessage("Expected targetDateRange to be $expected, but was $targetDateRange")
      }
    }
    return this
  }

  fun hasStatus(expected: StepStatus): StepResponseAssert {
    isNotNull
    with(actual!!) {
      if (status != expected) {
        failWithMessage("Expected status to be $expected, but was $status")
      }
    }
    return this
  }

  fun hasReference(expected: UUID): StepResponseAssert {
    isNotNull
    with(actual!!) {
      if (stepReference != expected) {
        failWithMessage("Expected reference to be $expected, but was $stepReference")
      }
    }
    return this
  }
}
