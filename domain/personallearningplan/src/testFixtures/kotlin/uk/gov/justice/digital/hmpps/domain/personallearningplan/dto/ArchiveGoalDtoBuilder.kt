package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import java.util.*

fun aValidArchiveGoalDto(
  reference: UUID = UUID.randomUUID(),
  reason: ReasonToArchiveGoal = ReasonToArchiveGoal.PRISONER_NO_LONGER_WANTS_TO_WORK_TOWARDS_GOAL,
  reasonOther: String? = null,
  prisonId: String = "BXI",
): ArchiveGoalDto = ArchiveGoalDto(
  reference = reference,
  reason = reason,
  reasonOther = reasonOther,
  prisonId = prisonId,
)
