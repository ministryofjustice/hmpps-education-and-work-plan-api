package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import java.time.LocalDate
import java.util.UUID

data class UpdateInductionScheduleStatusDto(
  val reference: UUID,
  val scheduleStatus: InductionScheduleStatus,
  val exemptionReason: String? = null,
  val latestDeadlineDate: LocalDate? = null,
  val prisonNumber: String,
)