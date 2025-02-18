package uk.gov.justice.digital.hmpps.domain.personallearningplan

import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus.ACTIVE
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import java.time.Instant
import java.time.LocalDate
import java.util.*

fun aValidGoal(
  reference: UUID = UUID.randomUUID(),
  title: String = "Improve communication skills",
  targetCompletionDate: LocalDate = LocalDate.now().plusMonths(6),
  steps: List<Step> = listOf(aValidStep(), anotherValidStep()),
  status: GoalStatus = ACTIVE,
  notes: String? = "Chris would like to improve his listening skills, not just his verbal communication",
  createdBy: String? = "asmith_gen",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
  archiveReason: ReasonToArchiveGoal? = null,
  archiveReasonOther: String? = null,
): Goal =
  Goal(
    reference = reference,
    title = title,
    targetCompletionDate = targetCompletionDate,
    steps = steps,
    status = status,
    notes = notes,
    createdBy = createdBy,
    createdAt = createdAt,
    createdAtPrison = createdAtPrison,
    lastUpdatedBy = lastUpdatedBy,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedAtPrison = lastUpdatedAtPrison,
    archiveReason = archiveReason,
    archiveReasonOther = archiveReasonOther,
  )
