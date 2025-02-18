package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService

private val log = KotlinLogging.logger {}

private const val REVIEW_SCHEDULE = "Review Schedule"
private const val INDUCTION_SCHEDULE = "Induction Schedule"

@Service
class PrisonerMergedEventService(
  private val reviewScheduleService: ReviewScheduleService,
  private val inductionScheduleService: InductionScheduleService,
) {
  fun process(
    inboundEvent: InboundEvent,
    additionalInformation: AdditionalInformation.PrisonerMergedAdditionalInformation,
  ) = with(additionalInformation) {
    log.info { "Processing Prisoner Merged event removed noms number [$removedNomsNumber]" }
    handle(
      REVIEW_SCHEDULE,
    ) { reviewScheduleService.exemptActiveReviewScheduleStatusDueToMerge(removedNomsNumber) }
    handle(
      INDUCTION_SCHEDULE,
    ) { inductionScheduleService.exemptActiveInductionScheduleStatusDueToMerge(removedNomsNumber) }
  }

  private fun handle(
    scheduleType: String,
    action: () -> Unit,
  ) {
    try {
      action()
    } catch (e: Exception) {
      if (e is ReviewScheduleNotFoundException || e is InductionScheduleNotFoundException) {
        log.debug { "Prisoner does not have an active $scheduleType; no need to set it as exempt" }
      } else {
        throw e // Re-throw unexpected exceptions
      }
    }
  }
}
