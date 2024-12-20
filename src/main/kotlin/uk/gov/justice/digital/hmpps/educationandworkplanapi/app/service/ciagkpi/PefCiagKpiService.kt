package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ciagkpi

import mu.KotlinLogging
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.CiagKpiService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.timeline.TimelineEventType
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ReviewScheduleAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TimelineEventFactory
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
  private val inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
  private val inductionPersistenceAdapter: InductionPersistenceAdapter,
  private val eventPublisher: EventPublisher,
  private val telemetryService: TelemetryService,
  private val timelineService: TimelineService,
  private val timelineEventFactory: TimelineEventFactory,
  private val reviewScheduleAdapter: ReviewScheduleAdapter,

) : CiagKpiService() {

  /**
   * Process an prisoner admission.
   *
   * - If the prisoner is brand new create a new Induction schedule
   * - If the prisoner already has an incomplete induction schedule (this will most likely be
   * a duplicate message) then do nothing.
   * - If the prisoner already has had their Induction created. Then this person is likely to
   * be a coming back into prison and needs to resume their Reviews.
   */
  /* TODO - the logic is wrong, and needs to be something like:
     if prisoner has no Induction, Action Plan or Induction Schedule, then create the Induction Schedule
     if prisoner has an Induction Schedule, and it is not complete, then return and do nothing
     If prisoner has an Induction Schedule that is complete, but has no Review Schedule, create the initial review schedule with status SCHEDULED, reason PRISONER_READMISSION and due date +20 days
     If prisoner has an Induction Schedule that is complete, and has a Review Schedule that is not complete, update the Review Schedule with status SCHEDULED, reason PRISONER_READMISSION and due date +20 days
     If prisoner has an Induction Schedule that is complete, and has a Review Schedule that is complete, create a new Review Schedule with status SCHEDULED, reason PRISONER_READMISSION and due date +20 days
   */

  override fun processPrisonerAdmission(prisonNumber: String, prisonAdmittedTo: String, eventDate: Instant) {
    log.info { "Creating or updating induction schedule for prisoner [$prisonNumber]" }
    if (activeInductionScheduleAlreadyExists(prisonNumber)) return

    if (inductionExists(prisonNumber)) {
      reviewScheduleAdapter.createInitialReviewScheduleIfInductionAndActionPlanExists(prisonNumber)
      return
    }

    createNewInductionSchedule(prisonNumber, eventDate)
  }

  private fun createNewInductionSchedule(prisonNumber: String, eventDate: Instant) {
    val inductionSchedule = inductionSchedulePersistenceAdapter.createInductionSchedule(
      CreateInductionScheduleDto(
        prisonNumber,
        calculateInductionDeadlineDate(prisonNumber, eventDate),
        InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
      ),
    )
    eventPublisher.createAndPublishInductionEvent(prisonNumber)
    telemetryService.trackInductionScheduleCreated(inductionSchedule)
    recordInductionTimelineEvent(inductionSchedule)
  }

  private fun activeInductionScheduleAlreadyExists(prisonNumber: String): Boolean {
    val existingSchedule = inductionSchedulePersistenceAdapter.getInductionSchedule(prisonNumber)
    if (existingSchedule != null && existingSchedule.scheduleStatus != InductionScheduleStatus.COMPLETE) {
      log.info { "Induction schedule already exists for prisoner [$prisonNumber], ignoring this message." }
      return true
    }
    return false
  }

  private fun inductionExists(prisonNumber: String): Boolean {
    return inductionPersistenceAdapter.getInduction(prisonNumber)?.let {
      log.info { "Induction already exists for prisoner [$prisonNumber]. Creating a review schedule." }
      true
    } ?: false
  }

  private fun recordInductionTimelineEvent(inductionSchedule: InductionSchedule) {
    val timelineEvent = timelineEventFactory.inductionScheduleTimelineEvent(
      inductionSchedule,
      TimelineEventType.INDUCTION_SCHEDULE_CREATED,
    )
    timelineService.recordTimelineEvent(inductionSchedule.prisonNumber, timelineEvent)
  }

  // This function will need to calculate the deadline date initially this will be the date the prisoner entered
  // prison plus an agreed number of days.
  override fun calculateInductionDeadlineDate(prisonNumber: String, eventDate: Instant): LocalDate {
    val europeLondon: ZoneId = ZoneId.of("Europe/London")
    val numberOfDaysToAdd = 20
    return eventDate.atZone(europeLondon).toLocalDate().plusDays(numberOfDaysToAdd.toLong())
  }
}
