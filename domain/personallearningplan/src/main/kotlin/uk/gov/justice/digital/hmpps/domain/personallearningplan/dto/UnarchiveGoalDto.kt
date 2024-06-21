package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import java.util.*

/**
 * A DTO class that contains the data required to unarchive an existing Goal domain object
 */
data class UnarchiveGoalDto(
  val reference: UUID,
)
