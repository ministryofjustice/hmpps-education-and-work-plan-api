package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert

fun assertThat(actual: ActionPlanSummaryListResponse?) = ActionPlanSummaryListResponseAssert(actual)

/**
 * AssertJ custom assertion for [ActionPlanSummaryListResponse]
 */
class ActionPlanSummaryListResponseAssert(actual: ActionPlanSummaryListResponse?) :
  AbstractObjectAssert<ActionPlanSummaryListResponseAssert, ActionPlanSummaryListResponse?>(actual, ActionPlanSummaryListResponseAssert::class.java) {

  fun hasSummaryCount(size: Int): ActionPlanSummaryListResponseAssert {
    isNotNull
    with(actual!!) {
      if (actionPlanSummaries.size != size) {
        failWithMessage("Expected actionPlanSummaries to have $size entries, but has ${actionPlanSummaries.size}")
      }
    }
    return this
  }

  fun hasEmptySummaries(): ActionPlanSummaryListResponseAssert {
    isNotNull
    with(actual!!) {
      if (actionPlanSummaries.isNotEmpty()) {
        failWithMessage("Expected actionPlanSummaries to be empty, but has ${actionPlanSummaries.size} entries")
      }
    }
    return this
  }
}
