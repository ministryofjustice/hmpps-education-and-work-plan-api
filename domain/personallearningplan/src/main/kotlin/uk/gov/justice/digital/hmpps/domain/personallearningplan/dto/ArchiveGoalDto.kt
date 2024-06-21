package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import java.util.*

/**
 * A DTO class that contains the data required to archive an existing Goal domain object
 */
data class ArchiveGoalDto(
  val reference: UUID,
  val reason: ReasonToArchiveGoal,
  val reasonOther: String?,
)

enum class ReasonToArchiveGoal {
  PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL,
  PRISONER_NO_LONGER_WANTS_TO_WORK_WITH_CIAG,
  SUITABLE_ACTIVITIES_NOT_AVAILABLE_IN_THIS_PRISON,
  OTHER,
}
