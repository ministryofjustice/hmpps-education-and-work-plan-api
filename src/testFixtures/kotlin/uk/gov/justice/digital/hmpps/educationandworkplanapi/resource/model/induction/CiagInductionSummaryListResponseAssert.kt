package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionSummaryListResponse

fun assertThat(actual: CiagInductionSummaryListResponse?) = CiagInductionSummaryListResponseAssert(actual)

/**
 * AssertJ custom assertion for [CiagInductionSummaryListResponse]
 */
class CiagInductionSummaryListResponseAssert(actual: CiagInductionSummaryListResponse?) :
  AbstractObjectAssert<CiagInductionSummaryListResponseAssert, CiagInductionSummaryListResponse?>(
    actual,
    CiagInductionSummaryListResponseAssert::class.java,
  ) {

  fun hasSummaryCount(size: Int): CiagInductionSummaryListResponseAssert {
    isNotNull
    with(actual!!) {
      if (ciagProfileList.size != size) {
        failWithMessage("Expected ciagProfileList to have $size entries, but has ${ciagProfileList.size}")
      }
    }
    return this
  }

  fun hasEmptySummaries(): CiagInductionSummaryListResponseAssert {
    isNotNull
    with(actual!!) {
      if (ciagProfileList.isNotEmpty()) {
        failWithMessage("Expected ciagProfileList to be empty, but has ${ciagProfileList.size} entries")
      }
    }
    return this
  }
}
