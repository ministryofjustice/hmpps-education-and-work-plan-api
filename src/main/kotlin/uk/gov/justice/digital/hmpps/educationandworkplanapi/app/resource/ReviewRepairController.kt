package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.review.ReviewScheduleStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ReviewScheduleHistoryRepository
import java.time.LocalDate

private val log = KotlinLogging.logger {}

/**
 * Temporary controller class for fixing review schedule records
 */
@Hidden
@RestController
class ReviewRepairController(
  private val reviewScheduleService: ReviewScheduleService,
  private val reviewScheduleHistoryRepository: ReviewScheduleHistoryRepository,
) {
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @GetMapping(value = ["/action-plans/schedules/earliest-start-date-repair/{prisonNumber}"])
  @Transactional
  fun fixEarliestStartDate(
    @PathVariable("prisonNumber") prisonNumber: String,
  ): FixResponse {
    log.info("Start: Fix Earliest start date for prisoner $prisonNumber")

    // get the review schedule history for the current review.
    val reviewSchedule = reviewScheduleService.getActiveReviewScheduleForPrisoner(prisonNumber)
    val reviewReference = reviewSchedule.reference
    val reviewScheduleHistory =
      reviewScheduleHistoryRepository.findAllByReference(reviewReference).sortedByDescending { it.version }
    val earliestExemption = reviewScheduleHistory
      .filter { it.scheduleStatus == ReviewScheduleStatus.EXEMPT_TEMP_ABSENCE }
      .minByOrNull { it.version }
      ?: throw ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "EXEMPT_TEMP_ABSENCE not found for prisoner $prisonNumber",
      )

    val updatedEarliestStartDate = earliestExemption.earliestReviewDate
    val previousEarliestStartDate = reviewSchedule.reviewScheduleWindow.dateFrom
    val performUpdate = updatedEarliestStartDate != previousEarliestStartDate
    if (performUpdate) {
      log.info("updating earliest start date for prisoner $prisonNumber from $previousEarliestStartDate to $updatedEarliestStartDate")
      // TODO - actually update the review start date and history and produce messages
    } else {
      log.info("not updating earliest start date for prisoner $prisonNumber")
    }

    log.info("End: Fix Earliest start date for prisoner $prisonNumber")

    return FixResponse(
      prisonNumber = prisonNumber,
      previousEarliestStartDate = previousEarliestStartDate,
      updatedEarliestStartDate = updatedEarliestStartDate,
      hasUpdatedEarliestStartDate = performUpdate,
    )
  }
}

data class FixResponse(
  val prisonNumber: String,
  val hasUpdatedEarliestStartDate: Boolean = false,
  val previousEarliestStartDate: LocalDate? = null,
  val updatedEarliestStartDate: LocalDate? = null,
)
