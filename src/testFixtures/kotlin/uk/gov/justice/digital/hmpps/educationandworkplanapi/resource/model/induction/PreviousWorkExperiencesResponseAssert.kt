package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.HasWorkedBefore
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PreviousWorkExperiencesResponse

fun assertThat(actual: PreviousWorkExperiencesResponse?) = PreviousWorkExperiencesResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [PreviousWorkExperiencesResponse].
 */
class PreviousWorkExperiencesResponseAssert(actual: PreviousWorkExperiencesResponse?) :
  AbstractObjectAssert<PreviousWorkExperiencesResponseAssert, PreviousWorkExperiencesResponse?>(
    actual,
    PreviousWorkExperiencesResponseAssert::class.java,
  ) {

  fun hasWorkedBefore(expected: HasWorkedBefore): PreviousWorkExperiencesResponseAssert {
    isNotNull
    with(actual!!) {
      if (hasWorkedBefore != expected) {
        failWithMessage("Expected hasWorkedBefore to be $expected, but was $hasWorkedBefore")
      }
    }
    return this
  }

  fun hasWorkedBeforeNotRelevantReason(expected: String): PreviousWorkExperiencesResponseAssert {
    isNotNull
    with(actual!!) {
      if (hasWorkedBeforeNotRelevantReason != expected) {
        failWithMessage("Expected hasWorkedBeforeNotRelevantReason to be $expected, but was $hasWorkedBeforeNotRelevantReason")
      }
    }
    return this
  }

  fun doesNotHaveWorkedBeforeNotRelevantReason(): PreviousWorkExperiencesResponseAssert {
    isNotNull
    with(actual!!) {
      if (hasWorkedBeforeNotRelevantReason != null) {
        failWithMessage("Expected hasWorkedBeforeNotRelevantReason to be null, but was $hasWorkedBeforeNotRelevantReason")
      }
    }
    return this
  }
}
