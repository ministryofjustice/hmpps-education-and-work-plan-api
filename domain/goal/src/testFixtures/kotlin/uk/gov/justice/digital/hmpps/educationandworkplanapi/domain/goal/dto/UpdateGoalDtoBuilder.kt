package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE
import java.time.LocalDate
import java.util.UUID

fun aValidUpdateGoalDto(
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  prisonId: String = "BXI",
  targetCompletionDate: LocalDate? = null,
  steps: List<UpdateStepDto> = listOf(aValidUpdateStepDto(), anotherValidUpdateStepDto()),
  status: GoalStatus = ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
): UpdateGoalDto =
  UpdateGoalDto(
    reference = reference,
    title = title,
    prisonId = prisonId,
    targetCompletionDate = targetCompletionDate,
    steps = steps,
    status = status,
    notes = notes,
  )
