package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate

fun aValidCreateGoalRequest(
  title: String = "Improve communication skills",
  reviewDate: LocalDate = LocalDate.now().plusMonths(6),
  category: GoalCategory = GoalCategory.PERSONAL_DEVELOPMENT,
  steps: List<CreateStepRequest> = listOf(aValidCreateStepRequest(), anotherValidCreateStepRequest()),
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
): CreateGoalRequest =
  CreateGoalRequest(
    title = title,
    reviewDate = reviewDate,
    category = category,
    steps = steps,
    notes = notes,
  )

fun anotherValidCreateGoalRequest(
  title: String = "Learn bricklaying",
  reviewDate: LocalDate = LocalDate.now().plusMonths(6),
  category: GoalCategory = GoalCategory.WORK,
  steps: List<CreateStepRequest> = listOf(aValidCreateStepRequest("Attend in house bricklaying course")),
  notes: String? = "",
): CreateGoalRequest =
  CreateGoalRequest(
    title = title,
    reviewDate = reviewDate,
    category = category,
    steps = steps,
    notes = notes,
  )
