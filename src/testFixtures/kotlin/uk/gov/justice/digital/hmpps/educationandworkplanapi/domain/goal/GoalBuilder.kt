package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidGoal(
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  reviewDate: LocalDate? = null,
  steps: List<Step> = listOf(aValidStep(), anotherValidStep()),
  status: GoalStatus = ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
): Goal =
  Goal(
    reference = reference,
    title = title,
    reviewDate = reviewDate,
    steps = steps,
    status = status,
    notes = notes,
    createdBy = createdBy,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedByDisplayName = lastUpdatedByDisplayName,
    lastUpdatedAt = lastUpdatedAt,
  )
