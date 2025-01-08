package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ciagkpi

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.CiagKpiService
import java.time.Instant
import java.time.LocalDate

/**
 * Implementation of the [CiagKpiService] with PES (from October '25) specific behaviours
 */
class PesCiagKpiService : CiagKpiService() {
  override fun processPrisonerAdmission(prisonNumber: String, prisonAdmittedTo: String, eventDate: Instant) {
    TODO("Not yet implemented")
  }

  override fun calculateInductionDeadlineDate(prisonNumber: String, eventDate: Instant): LocalDate {
    TODO("Not yet implemented")
  }
}
