package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
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
