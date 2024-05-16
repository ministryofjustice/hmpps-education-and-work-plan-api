package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import java.time.LocalDate
import java.util.UUID

/**
 * A DTO class that contains the data required to update an existing Goal domain object
 */
class UpdateGoalDto(
  val reference: UUID,
  val title: String,
  val prisonId: String,
  val targetCompletionDate: LocalDate,
  var status: GoalStatus = GoalStatus.ACTIVE,
  val notes: String? = null,
  steps: List<UpdateStepDto>,
) {
  val steps: List<UpdateStepDto>

  init {
    this.steps = steps
      .sortedBy { it.sequenceNumber } // sort by current sequence number (that may include gaps if steps have been removed)
      .mapIndexed { idx, step -> step.copy(sequenceNumber = idx + 1) } // map each step to a copy of itself with the sequential sequence number
  }
}
