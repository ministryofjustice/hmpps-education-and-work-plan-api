package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.PrisonerNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.Identifier
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.InboundEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.InboundEventsService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.PersonReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchApiService
import java.time.Instant

private val log = KotlinLogging.logger {}

/**
 * Temporary controller class for repairing SCHEDULED records when they should be RELEASED
 */
@Hidden
@RestController
class ReleasedPrisonerRepairController(
  private val inboundEventsService: InboundEventsService,
  private val objectMapper: ObjectMapper,
  private val prisonerSearchApiService: PrisonerSearchApiService,
) {
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_EDIT_REVIEWS)
  @GetMapping(value = ["/action-plans/schedules/fix-release/{prisonNumber}"])
  @Transactional
  fun fixRelease(
    @PathVariable("prisonNumber") prisonNumber: String,
    @RequestParam(name = "releasedToHospital", required = false, defaultValue = "false")
    releasedToHospital: Boolean,
  ) {
    var sendMessage = false
    log.info("fixing prisoner: $prisonNumber (releasedToHospital=$releasedToHospital)")
    try {
      val prisoner = prisonerSearchApiService.getPrisoner(prisonNumber)
      if (prisoner.inOutStatus.isNullOrBlank()) {
        log.info("couldn't determine in out status of: $prisonNumber not processing")
      } else {
        sendMessage = true
      }
    } catch (e: PrisonerNotFoundException) {
      log.info("Prisoner not found: $prisonNumber continue to process.")
      sendMessage = true
    } catch (e: Exception) {
      log.info("Some exception looking up prisoner $prisonNumber not processing.")
    }

    if (sendMessage) {
      inboundEventsService.process(
        inboundEvent = InboundEvent(
          EventType.PRISONER_RELEASED_FROM_PRISON,
          personReference = PersonReference(
            identifiers = listOf(Identifier("NOMS", prisonNumber)),
          ),
          additionalInformation = objectMapper.writeValueAsString(
            additionalInformation(prisonNumber, releasedToHospital),
          ),
          occurredAt = Instant.now(),
          publishedAt = Instant.now(),
          description = "Test message to correct data",
          version = "1",
        ),
      )

      log.info("fixed prisoner: $prisonNumber")
    }
  }

  private fun additionalInformation(
    prisonNumber: String,
    releasedToHospital: Boolean,
  ): PrisonerReleasedAdditionalInformation = PrisonerReleasedAdditionalInformation(
    nomsNumber = prisonNumber,
    reason = if (releasedToHospital) {
      PrisonerReleasedAdditionalInformation.Reason.RELEASED_TO_HOSPITAL
    } else {
      PrisonerReleasedAdditionalInformation.Reason.RELEASED
    },
    prisonId = "OUT",
    nomisMovementReasonCode = "nomisMovementReasonCode",
    details = null,
    currentLocation = null,
    currentPrisonStatus = null,
  )
}
