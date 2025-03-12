package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import java.time.LocalDate

fun aValidCreateInductionScheduleDto(
  prisonNumber: String = randomValidPrisonNumber(),
  deadlineDate: LocalDate = LocalDate.now().plusDays(20),
  scheduleCalculationRule: InductionScheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
  scheduleStatus: InductionScheduleStatus = InductionScheduleStatus.SCHEDULED,
  prisonId: String = "BXI",
): CreateInductionScheduleDto =
  CreateInductionScheduleDto(
    prisonNumber = prisonNumber,
    deadlineDate = deadlineDate,
    scheduleCalculationRule = scheduleCalculationRule,
    scheduleStatus = scheduleStatus,
    prisonId = prisonId,

  )
