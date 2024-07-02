package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal

sealed class GetGoalsResult {
  data class PrisonerNotFound(val prisonNumber: String) : GetGoalsResult() {
    fun errorMessage() =
      "No goals have been created for prisoner [$prisonNumber] yet"
  }

  data class Success(val goals: List<Goal>) : GetGoalsResult()
}
