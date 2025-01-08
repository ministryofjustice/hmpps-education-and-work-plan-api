package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service

import java.time.Instant
import java.time.LocalDate

/**
 * Abstract superclass for CIAG KPI service methods
 */

abstract class CiagKpiService {

  abstract fun processPrisonerAdmission(prisonNumber: String, prisonAdmittedTo: String, eventDate: Instant)

  abstract fun calculateInductionDeadlineDate(prisonNumber: String, eventDate: Instant): LocalDate
}
