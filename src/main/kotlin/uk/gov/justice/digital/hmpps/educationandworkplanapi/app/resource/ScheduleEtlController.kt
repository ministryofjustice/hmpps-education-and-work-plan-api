package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher
import java.time.LocalDate

private val log = KotlinLogging.logger {}

/**
 * Temporary controller class exposing REST API method that triggers the ETL of ReviewSchedule and InductionSchedule records
 */
@Hidden
@RestController
class ScheduleEtlController(
  private val eventPublisher: EventPublisher,
  private val inductionScheduleService: InductionScheduleService,
  private val reviewScheduleService: ReviewScheduleService,
) {

  @PostMapping("/action-plans/schedules/reschedule-inductions-following-transfer")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun rescheduleInductionsFollowingTransfer(
    @RequestBody prisonNumbersRequest: PrisonNumbersRequest,
  ) {
    val featureFixedOn = LocalDate.parse("2026-04-08")

    prisonNumbersRequest.prisonNumbers.distinct().onEach { prisonNumber ->
      val schedules = inductionScheduleService.getInductionScheduleHistoryForPrisoner(prisonNumber)
      if (schedules.size > 1 &&
        with(schedules.first()) {
          scheduleStatus == InductionScheduleStatus.SCHEDULED && !deadlineDate.isAfter(featureFixedOn)
        } &&
        with(schedules[1]) {
          scheduleStatus == InductionScheduleStatus.EXEMPT_PRISONER_TRANSFER
        }
      ) {
        val prisonId = schedules.first().lastUpdatedAtPrison
        log.info { "Rescheduling induction for prisoner $prisonNumber following transfer to $prisonId" }
        inductionScheduleService.exemptAndReScheduleActiveInductionScheduleDueToPrisonerTransfer(
          prisonNumber = prisonNumber,
          prisonTransferredTo = prisonId,
        )
      } else {
        log.info { "Induction schedule for prisoner $prisonNumber is not in a state by which it should be rescheduled" }
      }
    }
  }

  @PostMapping("/action-plans/schedules/reschedule-reviews-following-transfer")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun rescheduleReviewsFollowingTransfer(
    @RequestBody prisonNumbersRequest: PrisonNumbersRequest,
  ) {
    val featureFixedOn = LocalDate.parse("2026-04-08")

    prisonNumbersRequest.prisonNumbers.distinct().onEach { prisonNumber ->
      runCatching { reviewScheduleService.getActiveReviewScheduleForPrisoner(prisonNumber) }.getOrNull()
        ?.run {
          val scheduleHistoryForActiveReview = reviewScheduleService.getReviewSchedulesForPrisoner(prisonNumber).filter { it.reference == reference }

          if (scheduleHistoryForActiveReview.size > 1 &&
            with(scheduleHistoryForActiveReview.first()) {
              scheduleStatus == ReviewScheduleStatus.SCHEDULED && !latestReviewDate.isAfter(featureFixedOn)
            } &&
            with(scheduleHistoryForActiveReview[1]) {
              scheduleStatus == ReviewScheduleStatus.EXEMPT_PRISONER_TRANSFER
            }
          ) {
            val prisonId = scheduleHistoryForActiveReview.first().lastUpdatedAtPrison
            log.info { "Rescheduling review for prisoner $prisonNumber following transfer to $prisonId" }
            reviewScheduleService.exemptAndReScheduleActiveReviewScheduleDueToPrisonerTransfer(
              prisonNumber = prisonNumber,
              prisonTransferredTo = prisonId,
            )
          } else {
            log.info { "Review schedule for prisoner $prisonNumber is not in a state by which it should be rescheduled" }
          }
        } ?: run { log.info { "Review schedule for prisoner $prisonNumber not found" } }
    }
  }

  @PostMapping("/action-plans/schedules/publish-review-messages")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun generateReviewMessages(
    @RequestBody prisonNumbersRequest: PrisonNumbersRequest,
  ) {
    val reviewPrisonNumbers = prisonNumbersRequest.prisonNumbers.distinct()
    reviewPrisonNumbers.forEach(eventPublisher::createAndPublishReviewScheduleEvent)
  }

  @PostMapping("/action-plans/schedules/publish-induction-messages")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun generateInductionMessages(
    @RequestBody prisonNumbersRequest: PrisonNumbersRequest,
  ) {
    val inductionPrisonNumbers = prisonNumbersRequest.prisonNumbers.distinct()
    inductionPrisonNumbers.forEach(eventPublisher::createAndPublishInductionEvent)
  }
}

data class PrisonNumbersRequest(
  val prisonNumbers: List<String>,
)
