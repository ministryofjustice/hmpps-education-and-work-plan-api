package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.NOT_STARTED
import java.time.LocalDate
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
  val targetDate: LocalDate,
  val status: StepStatus = NOT_STARTED,
  val sequenceNumber: Int,
)