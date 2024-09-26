package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto

import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import java.time.LocalDate

fun aValidCreateInductionScheduleDto(
  prisonNumber: String = aValidPrisonNumber(),
  deadlineDate: LocalDate = LocalDate.now().plusDays(30),
  scheduleCalculationRule: InductionScheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
): CreateInductionScheduleDto =
  CreateInductionScheduleDto(
    prisonNumber = prisonNumber,
    deadlineDate = deadlineDate,
    scheduleCalculationRule = scheduleCalculationRule,
  )
