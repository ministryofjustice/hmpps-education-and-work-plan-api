package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalCategory.PERSONAL_DEVELOPMENT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidGoal(
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  reviewDate: LocalDate = LocalDate.now().plusMonths(6),
  category: GoalCategory = PERSONAL_DEVELOPMENT,
  steps: List<Step> = listOf(aValidStep(), anotherValidStep()),
  status: GoalStatus = ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  createdBy: String = "",
  createdAt: Instant = Instant.now(),
  lastUpdatedBy: String = "",
  lastUpdatedAt: Instant = Instant.now(),
): Goal =
  Goal(
    reference = reference,
    title = title,
    reviewDate = reviewDate,
    category = category,
    steps = steps,
    status = status,
    notes = notes,
    createdBy = createdBy,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
  )
