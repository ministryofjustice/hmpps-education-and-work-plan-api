package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalCategory.PERSONAL_DEVELOPMENT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalStatus.ACTIVE
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun aValidGoalEntity(
  id: UUID? = UUID.randomUUID(),
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  reviewDate: LocalDate = LocalDate.now().plusMonths(6),
  category: GoalCategory = PERSONAL_DEVELOPMENT,
  status: GoalStatus = ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  steps: MutableList<StepEntity> = mutableListOf(aValidStepEntity()),
  createdAt: Instant? = Instant.now(),
  createdBy: String? = "a.user.id",
  updatedAt: Instant? = Instant.now(),
  updatedBy: String? = "another.user.id",
): GoalEntity =
  GoalEntity(
    id = id,
    reference = reference,
    title = title,
    reviewDate = reviewDate,
    category = category,
    status = status,
    notes = notes,
    steps = steps,
    createdAt = createdAt,
    createdBy = createdBy,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
  )
