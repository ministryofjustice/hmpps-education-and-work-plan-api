package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate

fun aValidCreateGoalRequest(
  title: String = "Improve communication skills",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<CreateStepRequest> = listOf(aValidCreateStepRequest(), anotherValidCreateStepRequest()),
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  prisonId: String = "BXI",
): CreateGoalRequest =
  CreateGoalRequest(
    title = title,
    targetCompletionDate = targetCompletionDate,
    steps = steps,
    notes = notes,
    prisonId = prisonId,
  )

fun anotherValidCreateGoalRequest(
  title: String = "Learn bricklaying",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<CreateStepRequest> = listOf(aValidCreateStepRequest("Attend in house bricklaying course")),
  notes: String? = "",
  prisonId: String = "BXI",
): CreateGoalRequest =
  CreateGoalRequest(
    title = title,
    targetCompletionDate = targetCompletionDate,
    steps = steps,
    notes = notes,
    prisonId = prisonId,
  )
