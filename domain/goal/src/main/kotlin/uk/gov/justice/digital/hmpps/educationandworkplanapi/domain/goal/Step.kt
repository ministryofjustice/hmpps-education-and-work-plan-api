package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import java.util.UUID

/**
 * Represents an individual Step (or task) within a Goal. For example there could be a number of Steps to take before
 * the Goal itself can be considered complete, such as booking a course, doing the course and then taking an exam.
 *
 * Each Step has its own lifecycle (such as active or completed) and are ordered sequentially within the parent Goal.
 */
data class Step(
  val reference: UUID,
  val title: String,
  val targetDateRange: TargetDateRange?,
  val status: StepStatus = StepStatus.NOT_STARTED,
  val sequenceNumber: Int,
)

enum class TargetDateRange {
  ZERO_TO_THREE_MONTHS,
  THREE_TO_SIX_MONTHS,
  SIX_TO_TWELVE_MONTHS,
  MORE_THAN_TWELVE_MONTHS,
}

enum class StepStatus {
  NOT_STARTED,
  ACTIVE,
  COMPLETE,
}
