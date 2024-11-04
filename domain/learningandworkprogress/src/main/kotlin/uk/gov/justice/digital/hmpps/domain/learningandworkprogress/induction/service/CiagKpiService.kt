package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import java.time.Instant
import java.time.LocalDate

/**
 * Abstract superclass for CIAG KPI service methods
 */

private val log = KotlinLogging.logger {}
abstract class CiagKpiService {

  abstract fun processPrisonerAdmission(prisonNumber: String, prisonAdmittedTo: String, eventDate: Instant)
  fun processPrisonerTransfer(prisonNumber: String, prisonTransferredTo: String) {
    // TODO - implement
  }

  fun determineInductionScheduleCalculationRule(prisonNumber: String): InductionScheduleCalculationRule {
    // TODO
    // Based on how long the prisoner has left to serve set the calculation rule
    // get the prisoners release date and use it to determine which of the following rules to apply:
    //    NEW_PRISON_ADMISSION
    //    EXISTING_PRISONER_LESS_THAN_6_MONTHS_TO_SERVE
    //    EXISTING_PRISONER_BETWEEN_6_AND_12_MONTHS_TO_SERVE
    //    EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE
    //    EXISTING_PRISONER_INDETERMINATE_SENTENCE
    //    EXISTING_PRISONER_ON_REMAND
    //    EXISTING_PRISONER_UN_SENTENCED

    // return a default one for now:
    return InductionScheduleCalculationRule.EXISTING_PRISONER_BETWEEN_12_AND_60_MONTHS_TO_SERVE
  }

  abstract fun calculateInductionDeadlineDate(prisonNumber: String, eventDate: Instant): LocalDate
}
