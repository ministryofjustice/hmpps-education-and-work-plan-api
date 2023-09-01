package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert
import java.time.LocalDate

fun assertThat(actual: ActionPlanSummaryResponse?) = ActionPlanSummaryResponseAssert(actual)

/**
 * AssertJ custom assertion for [ActionPlanSummaryResponse]
 */
class ActionPlanSummaryResponseAssert(actual: ActionPlanSummaryResponse?) :
  AbstractObjectAssert<ActionPlanSummaryResponseAssert, ActionPlanSummaryResponse?>(
    actual,
    ActionPlanSummaryResponseAssert::class.java,
  ) {

  fun hasPrisonNumber(expected: String): ActionPlanSummaryResponseAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun hasReviewDate(expected: LocalDate): ActionPlanSummaryResponseAssert {
    isNotNull
    with(actual!!) {
      if (reviewDate != expected) {
        failWithMessage("Expected reviewDate to be $expected, but was $reviewDate")
      }
    }
    return this
  }

  fun hasNoReviewDate(): ActionPlanSummaryResponseAssert {
    isNotNull
    with(actual!!) {
      if (reviewDate != null) {
        failWithMessage("Expected reviewDate to be null, but was $reviewDate")
      }
    }
    return this
  }
}
