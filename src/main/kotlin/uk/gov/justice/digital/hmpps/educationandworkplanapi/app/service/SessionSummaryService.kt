package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSummaryResponse
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Service
class SessionSummaryService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val reviewScheduleService: ReviewScheduleService,
  private val inductionScheduleService: InductionScheduleService,
) {

  fun getSessionSummaries(prisonId: String): SessionSummaryResponse {
    val today = LocalDate.now()
    val prisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId)

    val dueReviews = mutableListOf<Prisoner>()
    val overdueReviews = mutableListOf<Prisoner>()
    val exemptReviews = mutableListOf<Prisoner>()
    val dueInductions = mutableListOf<Prisoner>()
    val overdueInductions = mutableListOf<Prisoner>()
    val exemptInductions = mutableListOf<Prisoner>()

    prisoners.forEach { prisoner ->
      val inductionSchedule =
        runCatching { inductionScheduleService.getInductionScheduleForPrisoner(prisoner.prisonerNumber) }.getOrNull()
      val reviewSchedule =
        runCatching { reviewScheduleService.getLatestReviewScheduleForPrisoner(prisoner.prisonerNumber) }.getOrNull()

      if (inductionSchedule != null && (inductionSchedule.scheduleStatus != InductionScheduleStatus.COMPLETED) && reviewSchedule != null && (reviewSchedule.scheduleStatus != ReviewScheduleStatus.COMPLETED)) {
        log.warn("Prisoner ${prisoner.prisonerNumber} has both an incomplete induction schedule and review schedule.")
      }

      inductionSchedule?.let {
        if (inductionSchedule.scheduleStatus != InductionScheduleStatus.COMPLETED) {
          when {
            inductionSchedule.scheduleStatus.includeExceptionOnSummary() -> exemptInductions.add(prisoner)
            inductionSchedule.scheduleStatus == InductionScheduleStatus.SCHEDULED &&
              inductionSchedule.deadlineDate < today -> overdueInductions.add(prisoner)
            inductionSchedule.scheduleStatus == InductionScheduleStatus.SCHEDULED &&
              inductionSchedule.deadlineDate > today -> dueInductions.add(prisoner)
          }
        }
      }

      reviewSchedule?.let {
        if (reviewSchedule.scheduleStatus != ReviewScheduleStatus.COMPLETED) {
          when {
            reviewSchedule.scheduleStatus.includeExceptionOnSummary() -> exemptReviews.add(prisoner)
            reviewSchedule.scheduleStatus == ReviewScheduleStatus.SCHEDULED &&
              reviewSchedule.reviewScheduleWindow.dateTo < today -> overdueReviews.add(prisoner)
            reviewSchedule.scheduleStatus == ReviewScheduleStatus.SCHEDULED &&
              reviewSchedule.reviewScheduleWindow.dateTo > today -> dueReviews.add(prisoner)
          }
        }
      }
    }

    return SessionSummaryResponse(
      dueReviews = dueReviews.size,
      overdueReviews = overdueReviews.size,
      exemptReviews = exemptReviews.size,
      dueInductions = dueInductions.size,
      overdueInductions = overdueInductions.size,
      exemptInductions = exemptInductions.size,
    )
  }
}
