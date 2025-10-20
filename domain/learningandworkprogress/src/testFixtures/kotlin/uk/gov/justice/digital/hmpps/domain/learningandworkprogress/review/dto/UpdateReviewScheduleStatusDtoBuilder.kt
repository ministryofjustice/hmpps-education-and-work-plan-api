package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate
import java.util.UUID

fun aValidUpdateReviewScheduleStatusDto(
  reference: UUID = UUID.randomUUID(),
  scheduleStatus: ReviewScheduleStatus = ReviewScheduleStatus.SCHEDULED,
  exemptionReason: String? = null,
  prisonId: String = "BXI",
  earliestReviewDate: LocalDate? = LocalDate.now(),
  latestReviewDate: LocalDate? = LocalDate.now().plusMonths(6),
  prisonNumber: String = randomValidPrisonNumber(),
): UpdateReviewScheduleStatusDto = UpdateReviewScheduleStatusDto(
  reference = reference,
  scheduleStatus = scheduleStatus,
  exemptionReason = exemptionReason,
  prisonId = prisonId,
  earliestReviewDate = earliestReviewDate,
  latestReviewDate = latestReviewDate,
  prisonNumber = prisonNumber,
)
