package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import java.time.LocalDate

data class CreateInductionScheduleDto(
  val prisonNumber: String,
  val deadlineDate: LocalDate,
  val scheduleCalculationRule: InductionScheduleCalculationRule,
  val scheduleStatus: InductionScheduleStatus,
  val prisonId: String,
)
