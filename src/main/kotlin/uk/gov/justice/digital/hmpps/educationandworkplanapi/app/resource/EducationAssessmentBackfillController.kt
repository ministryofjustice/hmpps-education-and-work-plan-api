package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.EducationAssessmentEventService
import java.time.LocalDate

private val log = KotlinLogging.logger {}

/**
 * Temporary controller to backfill historical education assessment events from App Insights.
 *
 * RR-2396: Retrospectively populate the database with assessment events that occurred between
 * 02/10/2025 and the deployment of the live SQS listener (RR-2368).
 *
 * Each event is processed through the same service as the SQS listener — including prisoner
 * lookup, entity persistence, timeline event, and telemetry.
 */
@Hidden
@RestController
class EducationAssessmentBackfillController(
  private val educationAssessmentEventService: EducationAssessmentEventService,
) {

  @PostMapping("/education-assessment-events/backfill")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  fun backfillAssessmentEvents(
    @RequestBody request: BackfillRequest,
  ): BackfillResponse {
    log.info { "Received backfill request with ${request.events.size} events" }

    // Process each event through the same service the listener uses
    var successCount = 0
    val failures = mutableListOf<BackfillFailure>()

    request.events.forEach { event ->
      try {
        educationAssessmentEventService.process(
          AssessmentEventDto(
            prisonNumber = event.prisonNumber,
            status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
            statusChangeDate = event.statusChangeDate,
            detailUrl = event.detailUrl,
          ),
        )
        successCount++
      } catch (e: Exception) {
        log.error(e) { "Failed to process backfill event for prisoner [${event.prisonNumber}]" }
        failures.add(BackfillFailure(event.prisonNumber, e.message ?: "Unknown error"))
      }
    }

    log.info { "Backfill complete: $successCount saved, ${failures.size} failed out of ${request.events.size} events" }

    with(
      BackfillResponse(
        totalReceived = request.events.size,
        totalSaved = successCount,
        failures = failures,
      ),
    ) {
      if (totalReceived > 0 && successCount == 0) {
        // if successCount is zero then no records in the request were processed successfully
        throw RuntimeException(this.toString())
      }
      return this
    }
  }

  data class BackfillRequest(
    val events: List<BackfillEvent>,
  )

  data class BackfillEvent(
    val prisonNumber: String,
    val statusChangeDate: LocalDate,
    val detailUrl: String? = null,
  )

  data class BackfillResponse(
    val totalReceived: Int,
    val totalSaved: Int,
    val failures: List<BackfillFailure>,
  )

  data class BackfillFailure(
    val prisonNumber: String,
    val error: String,
  )
}
