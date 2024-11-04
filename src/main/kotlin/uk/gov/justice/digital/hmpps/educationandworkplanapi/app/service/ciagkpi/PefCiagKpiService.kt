package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ciagkpi

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.CiagKpiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerSearchApiClient
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Implementation of the [CiagKpiService] with PEF (April '25 -> October '25) specific behaviours
 *
 * Enabled when the property `ciag-kpi-processing-rule` is set to `PEF` via [CiagKpiServiceFactory]
 */
class PefCiagKpiService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) : CiagKpiService() {

  // This function will need to calculate the deadline date initially this will be the date the prisoner entered
  // prison plus an agreed number of days.
  override fun calculateInductionDeadlineDate(prisonNumber: String, eventDate: Instant): LocalDate {
    val europeLondon: ZoneId = ZoneId.of("Europe/London")
    val numberOfDaysToAdd = 20
    return eventDate.atZone(europeLondon).toLocalDate().plusDays(numberOfDaysToAdd.toLong())
  }
}
