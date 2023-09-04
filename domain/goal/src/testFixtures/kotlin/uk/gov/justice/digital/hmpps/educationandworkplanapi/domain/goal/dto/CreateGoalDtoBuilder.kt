package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE
import java.time.LocalDate

fun aValidCreateGoalDto(
  title: String = "Improve communication skills",
  reviewDate: LocalDate? = null,
  steps: List<CreateStepDto> = listOf(aValidCreateStepDto(), anotherValidCreateStepDto()),
  status: GoalStatus = ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
): CreateGoalDto =
  CreateGoalDto(
    title = title,
    reviewDate = reviewDate,
    steps = steps,
    status = status,
    notes = notes,
  )
