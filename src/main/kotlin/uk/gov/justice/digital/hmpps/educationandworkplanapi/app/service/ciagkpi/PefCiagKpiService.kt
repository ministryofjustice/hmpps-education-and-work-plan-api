package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ciagkpi

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.CiagKpiService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerSearchApiClient
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val log = KotlinLogging.logger {}

/**
 * Implementation of the [CiagKpiService] with PEF (April '25 -> October '25) specific behaviours
 *
 * Enabled when the property `ciag-kpi-processing-rule` is set to `PEF` via [CiagKpiServiceFactory]
 */
class PefCiagKpiService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
  private val inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
  private val inductionPersistenceAdapter: InductionPersistenceAdapter,

) : CiagKpiService() {

  override fun processPrisonerAdmission(prisonNumber: String, prisonAdmittedTo: String, eventDate: Instant) {
    createOrUpdateInductionSchedule(prisonNumber, eventDate)
  }

  fun createOrUpdateInductionSchedule(prisonNumber: String, eventDate: Instant) {
    log.info { "Creating or updating induction schedule for prisoner [$prisonNumber]" }

    // Check if an induction schedule already exists.
    val existingSchedule = inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber)
    if (existingSchedule != null) {
      // Update existing schedule with the correct calculation rule and deadline date.
      val calculationRule = determineInductionScheduleCalculationRule(prisonNumber)
      val updatedDeadlineDate = calculateInductionDeadlineDate(prisonNumber, eventDate)

      inductionSchedulePersistenceAdapter.updateSchedule(
        prisonNumber,
        calculationRule,
        updatedDeadlineDate,
      )
      return
    }

    // If no induction schedule exists, check for an existing induction.
    if (inductionPersistenceAdapter.getInduction(prisonNumber) != null) {
      log.info { "Induction already exists for prisoner [$prisonNumber], creating a review." }
      // TODO: Implement review creation
      return
    }

    // Create a new induction schedule.
    inductionSchedulePersistenceAdapter.createInductionSchedule(
      CreateInductionScheduleDto(
        prisonNumber,
        calculateInductionDeadlineDate(prisonNumber, eventDate),
        InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
      ),
    )

//    TODO Implement follow on telemetry and outbound event generation
  }

  // This function will need to calculate the deadline date initially this will be the date the prisoner entered
  // prison plus an agreed number of days.
  override fun calculateInductionDeadlineDate(prisonNumber: String, eventDate: Instant): LocalDate {
    val europeLondon: ZoneId = ZoneId.of("Europe/London")
    val numberOfDaysToAdd = 20
    return eventDate.atZone(europeLondon).toLocalDate().plusDays(numberOfDaysToAdd.toLong())
  }
}