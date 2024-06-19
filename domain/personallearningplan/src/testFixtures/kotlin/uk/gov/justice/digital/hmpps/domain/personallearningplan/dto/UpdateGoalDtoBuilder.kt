package uk.gov.justice.digital.hmpps.domain.personallearningplan.dto

import java.time.LocalDate
import java.util.*

fun aValidUpdateGoalDto(
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  prisonId: String = "BXI",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<UpdateStepDto> = listOf(aValidUpdateStepDto(), anotherValidUpdateStepDto()),
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
): UpdateGoalDto =
  UpdateGoalDto(
    reference = reference,
    title = title,
    prisonId = prisonId,
    targetCompletionDate = targetCompletionDate,
    steps = steps,
    notes = notes,
  )
