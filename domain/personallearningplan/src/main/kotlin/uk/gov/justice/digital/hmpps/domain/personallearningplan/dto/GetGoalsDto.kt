package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus

data class GetGoalsDto(val prisonNumber: String, val statuses: Set<GoalStatus>?)
