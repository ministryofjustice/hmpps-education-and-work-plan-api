package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
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

    val dueReviews = mutableListOf<String>()
    val overdueReviews = mutableListOf<String>()
    val exemptReviews = mutableListOf<String>()
    val dueInductions = mutableListOf<String>()
    val overdueInductions = mutableListOf<String>()
    val exemptInductions = mutableListOf<String>()

    val prisonerNumbers = prisoners.map { it.prisonerNumber }

    val inductionSchedules = inductionScheduleService.getInCompleteInductionSchedules(prisonerNumbers)
    val reviewSchedules = reviewScheduleService.getInCompleteReviewSchedules(prisonerNumbers)

    inductionSchedules.forEach { schedule ->
      when {
        schedule.scheduleStatus.includeExceptionOnSummary() -> exemptInductions.add(schedule.prisonNumber)
        schedule.scheduleStatus == InductionScheduleStatus.SCHEDULED &&
          schedule.deadlineDate < today -> overdueInductions.add(schedule.prisonNumber)
        schedule.scheduleStatus == InductionScheduleStatus.SCHEDULED &&
          schedule.deadlineDate > today -> dueInductions.add(schedule.prisonNumber)
      }
    }

    reviewSchedules.forEach { schedule ->
      when {
        schedule.scheduleStatus.includeExceptionOnSummary() -> exemptReviews.add(schedule.prisonNumber)
        schedule.scheduleStatus == ReviewScheduleStatus.SCHEDULED &&
          schedule.reviewScheduleWindow.dateTo < today -> overdueReviews.add(schedule.prisonNumber)
        schedule.scheduleStatus == ReviewScheduleStatus.SCHEDULED &&
          schedule.reviewScheduleWindow.dateTo > today -> dueReviews.add(schedule.prisonNumber)
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
