package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate

fun aValidCreateGoalRequest(
  title: String = "Improve communication skills",
  reviewDate: LocalDate? = null,
  steps: List<StepRequest> = listOf(aValidCreateStepRequest(), anotherValidCreateStepRequest()),
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
): CreateGoalRequest =
  CreateGoalRequest(
    title = title,
    reviewDate = reviewDate,
    steps = steps,
    notes = notes,
  )

fun anotherValidCreateGoalRequest(
  title: String = "Learn bricklaying",
  reviewDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<StepRequest> = listOf(aValidCreateStepRequest("Attend in house bricklaying course")),
  notes: String? = "",
): CreateGoalRequest =
  CreateGoalRequest(
    title = title,
    reviewDate = reviewDate,
    steps = steps,
    notes = notes,
  )
