package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import java.time.LocalDate
import java.util.UUID

data class UpdateReviewScheduleStatusDto(
  val reference: UUID,
  val scheduleStatus: ReviewScheduleStatus,
  val exemptionReason: String? = null,
  val prisonId: String,
  val latestReviewDate: LocalDate? = null,
  val prisonNumber: String,
)
