package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus.ACTIVE
import java.time.LocalDate

fun aValidCreateGoalDto(
  title: String = "Improve communication skills",
  prisonId: String = "BXI",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<CreateStepDto> = listOf(aValidCreateStepDto(), anotherValidCreateStepDto()),
  status: GoalStatus = ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
): CreateGoalDto =
  CreateGoalDto(
    title = title,
    prisonId = prisonId,
    targetCompletionDate = targetCompletionDate,
    steps = steps,
    status = status,
    notes = notes,
  )
