package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus.SCHEDULED
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonerIdsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionStatusType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSummaryResponse
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus.SCHEDULED as INDUCTION_SCHEDULED

private val log = KotlinLogging.logger {}

@Service
class SessionSummaryService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val reviewScheduleService: ReviewScheduleService,
  private val inductionScheduleService: InductionScheduleService,
) {

  /**
   * returns due, overdue and on hold counts for Inductions and Reviews
   * for all prisoners within a given prison.
   */
  fun getSessionSummaries(prisonId: String): SessionSummaryResponse {
    val prisoners = prisonerSearchApiService.getAllPrisonersInPrison(prisonId)
      .also {
        log.debug { "${it.size} prisoners returned from prisoner-search-api" }
      }
    val prisonerNumbers = prisoners.map { it.prisonerNumber }.toSet().sorted().toList()
      .also {
        log.debug { "${it.size} unique prisoners returned from prisoner-search-api" }
      }
    val sessionSummaries = getSessionSummaries(prisonerNumbers)

    return SessionSummaryResponse(
      dueReviews = sessionSummaries.dueReviews.size,
      overdueReviews = sessionSummaries.overdueReviews.size,
      exemptReviews = sessionSummaries.exemptReviews.size,
      dueInductions = sessionSummaries.dueInductions.size,
      overdueInductions = sessionSummaries.overdueInductions.size,
      exemptInductions = sessionSummaries.exemptInductions.size,
    )
  }

  /**
   * returns a list of Induction and Review session information
   * for all prisoners from the given list. The list is filtered on SessionStatusType
   * which can be DUE, OVERDUE and ON_HOLD
   */
  fun getSessions(status: SessionStatusType, requestIds: PrisonerIdsRequest): SessionResponses {
    val sessionSummaries = getSessionSummaries(requestIds.prisonNumbers)

    val sessions = when (status) {
      SessionStatusType.DUE -> mapSessions(sessionSummaries.dueReviews, sessionSummaries.dueInductions)
      SessionStatusType.OVERDUE -> mapSessions(sessionSummaries.overdueReviews, sessionSummaries.overdueInductions)
      SessionStatusType.ON_HOLD -> mapSessions(sessionSummaries.exemptReviews, sessionSummaries.exemptInductions)
    }

    return SessionResponses(sessions = sessions)
  }

  fun mapSessions(
    reviews: List<ReviewSchedule>,
    inductions: List<InductionSchedule>,
  ): List<SessionResponse> = reviews.map {
    SessionResponse(
      sessionType = SessionResponse.SessionType.REVIEW,
      prisonNumber = it.prisonNumber,
      deadlineDate = it.reviewScheduleWindow.dateTo,
      reference = it.reference,
      exemptionReason = if (it.scheduleStatus.isExemptionOrExclusion()) it.scheduleStatus.name else null,
      exemptionDate = if (it.scheduleStatus.isExemptionOrExclusion()) convertInstantToLocalDate(it.lastUpdatedAt) else null,
      scheduleCalculationRule = it.scheduleCalculationRule.name,
      followingTransfer = it.followingTransfer,
    )
  } + inductions.map {
    SessionResponse(
      sessionType = SessionResponse.SessionType.INDUCTION,
      prisonNumber = it.prisonNumber,
      deadlineDate = it.deadlineDate,
      reference = it.reference,
      exemptionReason = if (it.scheduleStatus.isExemptionOrExclusion()) it.scheduleStatus.name else null,
      exemptionDate = if (it.scheduleStatus.isExemptionOrExclusion()) convertInstantToLocalDate(it.lastUpdatedAt) else null,
      scheduleCalculationRule = it.scheduleCalculationRule.name,
    )
  }

  fun convertInstantToLocalDate(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate = instant.atZone(zoneId).toLocalDate()

  /**
   * Work out which schedules are due, overdue and on_hold (exempt)
   * and then pop each one in a bucket to be counted or filtered later.
   *
   * Not that some sessions are disregarded - these include EXEMPT due to death or TRANSFER
   * The complete list is configured in scheduleStatus.includeExceptionOnSummary()
   */
  private fun getSessionSummaries(
    prisonerNumbers: List<String>,
  ): SessionSummaries {
    val today = LocalDate.now()

    val sessionSummaries = SessionSummaries()
    val inductionSchedules = inductionScheduleService.getInCompleteInductionSchedules(prisonerNumbers)
    val reviewSchedules = reviewScheduleService.getInCompleteReviewSchedules(prisonerNumbers)

    inductionSchedules.forEach { schedule ->
      when {
        schedule.includeInExemptCount() -> sessionSummaries.exemptInductions.add(schedule)
        schedule.includeInOverdueCount(today) -> sessionSummaries.overdueInductions.add(schedule)
        schedule.includeInDueCount(today) -> sessionSummaries.dueInductions.add(schedule)
      }
    }

    reviewSchedules.forEach { schedule ->
      when {
        schedule.includeInExemptCount() -> sessionSummaries.exemptReviews.add(schedule)
        schedule.includeInOverdueCount(today) -> sessionSummaries.overdueReviews.add(schedule)
        schedule.includeInDueCount(today) -> sessionSummaries.dueReviews.add(schedule)
      }
    }

    return sessionSummaries
  }

  private data class SessionSummaries(
    val dueReviews: MutableList<ReviewSchedule> = mutableListOf(),
    val overdueReviews: MutableList<ReviewSchedule> = mutableListOf(),
    val exemptReviews: MutableList<ReviewSchedule> = mutableListOf(),
    val dueInductions: MutableList<InductionSchedule> = mutableListOf(),
    val overdueInductions: MutableList<InductionSchedule> = mutableListOf(),
    val exemptInductions: MutableList<InductionSchedule> = mutableListOf(),
  )

  /**
   * Returns true if the [InductionSchedule] is considered exempt for the purposes of the [SessionSummaries] counts
   */
  private fun InductionSchedule.includeInExemptCount(): Boolean = with(scheduleStatus) {
    isExemptionOrExclusion() && includeExemptionOnSummary
  }

  /**
   * Returns true if the [InductionSchedule] is considered overdue for the purposes of the [SessionSummaries] counts
   */
  private fun InductionSchedule.includeInOverdueCount(today: LocalDate): Boolean = with(scheduleStatus) {
    this == INDUCTION_SCHEDULED && deadlineDate.isBefore(today)
  }

  /**
   * Returns true if the [InductionSchedule] is considered due for the purposes of the [SessionSummaries] counts
   */
  private fun InductionSchedule.includeInDueCount(today: LocalDate): Boolean = with(scheduleStatus) {
    this == INDUCTION_SCHEDULED && today in deadlineDate.minusMonths(2)..deadlineDate
  }

  /**
   * Returns true if the [ReviewSchedule] is considered exempt for the purposes of the [SessionSummaries] counts
   */
  private fun ReviewSchedule.includeInExemptCount(): Boolean = with(scheduleStatus) {
    isExemptionOrExclusion() && includeExemptionOnSummary
  }

  /**
   * Returns true if the [ReviewSchedule] is considered overdue for the purposes of the [SessionSummaries] counts
   */
  private fun ReviewSchedule.includeInOverdueCount(today: LocalDate): Boolean = with(scheduleStatus) {
    this == SCHEDULED && reviewScheduleWindow.dateTo.isBefore(today)
  }

  /**
   * Returns true if the [ReviewSchedule] is considered due for the purposes of the [SessionSummaries] counts
   */
  private fun ReviewSchedule.includeInDueCount(today: LocalDate): Boolean = with(scheduleStatus) {
    this == SCHEDULED && today in reviewScheduleWindow.dateFrom..reviewScheduleWindow.dateTo
  }
}
