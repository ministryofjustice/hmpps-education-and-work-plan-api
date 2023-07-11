package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun aValidGoalResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  reviewDate: LocalDate? = null,
  category: GoalCategory = GoalCategory.PERSONAL_DEVELOPMENT,
  steps: List<StepResponse> = listOf(aValidStepResponse(), anotherValidStepResponse()),
  status: GoalStatus = GoalStatus.ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  createdBy: String = "",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  updatedBy: String = "",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
): GoalResponse =
  GoalResponse(
    goalReference = reference.toString(),
    title = title,
    reviewDate = reviewDate,
    category = category,
    steps = steps,
    status = status,
    notes = notes,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedBy = updatedBy,
    updatedAt = updatedAt,
  )

fun anotherValidGoalResponse(
  reference: UUID = UUID.randomUUID(),
  title: String = "Learn bricklaying",
  reviewDate: LocalDate = LocalDate.now().plusMonths(6),
  category: GoalCategory = GoalCategory.WORK,
  steps: List<StepResponse> = listOf(aValidStepResponse(title = "Attend in house bricklaying course")),
  status: GoalStatus = GoalStatus.ACTIVE,
  notes: String? = null,
  createdBy: String = "",
  createdAt: OffsetDateTime = OffsetDateTime.now(),
  updatedBy: String = "",
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
): GoalResponse =
  GoalResponse(
    goalReference = reference.toString(),
    title = title,
    reviewDate = reviewDate,
    category = category,
    steps = steps,
    status = status,
    notes = notes,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedBy = updatedBy,
    updatedAt = updatedAt,
  )
