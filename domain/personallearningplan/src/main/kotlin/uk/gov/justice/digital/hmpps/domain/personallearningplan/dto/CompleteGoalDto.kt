package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import java.util.*

/**
 * A DTO class that contains the data required to complete an existing Goal domain object
 */
data class CompleteGoalDto(
  val reference: UUID,
  val prisonId: String,
)
