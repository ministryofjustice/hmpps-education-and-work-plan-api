package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ciagkpi

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.CiagKpiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerSearchApiClient
import java.time.Instant
import java.time.LocalDate

/**
 * Implementation of the [CiagKpiService] with PES (from October '25) specific behaviours
 *
 * Enabled when the property `ciag-kpi-processing-rule` is set to `PES` via [CiagKpiServiceFactory]
 */
class PesCiagKpiService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) : CiagKpiService() {
  override fun calculateInductionDeadlineDate(prisonNumber: String, eventDate: Instant): LocalDate {
    TODO("Not yet implemented")
  }
}
