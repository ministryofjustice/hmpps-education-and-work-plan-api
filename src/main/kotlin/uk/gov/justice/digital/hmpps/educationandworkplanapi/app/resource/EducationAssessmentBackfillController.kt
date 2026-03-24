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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment.EducationAssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.EducationAssessmentEventRepository
import java.time.LocalDate
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * Temporary controller to backfill historical education assessment events from App Insights.
 *
 * RR-2396: Retrospectively populate the database with assessment events that occurred between
 * 02/10/2025 and the deployment of the live SQS listener (RR-2368).
 */
@Hidden
@RestController
class EducationAssessmentBackfillController(
  private val educationAssessmentEventRepository: EducationAssessmentEventRepository,
) {

  companion object {
    private const val BACKFILL_SOURCE = "APP_INSIGHTS"
    private const val BACKFILL_PRISON_ID = "N/A"
  }

  @PostMapping("/education-assessment-events/backfill")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @Transactional
  fun backfillAssessmentEvents(
    @RequestBody request: BackfillRequest,
  ): BackfillResponse {
    val totalReceived = request.events.size
    log.info { "Received backfill request with $totalReceived events" }

    // Deduplicate input by PRN + date
    val uniqueEvents = request.events.distinctBy { it.prisonNumber to it.statusChangeDate }
    log.info { "Deduplicated to ${uniqueEvents.size} unique events (removed ${totalReceived - uniqueEvents.size} duplicates)" }

    // Create entities
    val entities = uniqueEvents.map { event ->
      EducationAssessmentEventEntity(
        reference = UUID.randomUUID(),
        prisonNumber = event.prisonNumber,
        status = EducationAssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = event.statusChangeDate,
        source = BACKFILL_SOURCE,
        detailUrl = event.detailUrl,
        createdAtPrison = BACKFILL_PRISON_ID,
        updatedAtPrison = BACKFILL_PRISON_ID,
      )
    }

    // Batch save
    educationAssessmentEventRepository.saveAll(entities)
    log.info { "Saved ${entities.size} education assessment events from backfill" }

    return BackfillResponse(
      totalReceived = totalReceived,
      totalDeduplicatedInput = uniqueEvents.size,
      totalSaved = entities.size,
    )
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
    val totalDeduplicatedInput: Int,
    val totalSaved: Int,
  )
}
