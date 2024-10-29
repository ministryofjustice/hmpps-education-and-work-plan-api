package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionSchedule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleEventService

/**
 * Implementation of [AsyncInductionScheduleEventService] for performing additional asynchronous actions related to [InductionSchedule] events.
 */
@Component
@Async
class AsyncInductionScheduleEventService(
  private val telemetryService: TelemetryService,
) : InductionScheduleEventService {

  private val log = KotlinLogging.logger {}
  override fun inductionScheduleCreated(createdInductionSchedule: InductionSchedule) {
    log.debug { "Induction schedule created event for prisoner [${createdInductionSchedule.prisonNumber}]" }
    log.debug { "About to send induction schedule created message" }
    telemetryService.trackInductionScheduleCreated(inductionSchedule = createdInductionSchedule)
    // create the induction created message will look something like

    """
      {
        "eventType": "plp.induction-schedule.created",
        "version": 1,
        "description": "An induction schedule was created in plp",
        "detailUrl": "/v1/persons/{prisonerId}/plp-induction-schedule",
        "occurredAt": "2024-10-29T12:16:04+01:00",
        "additionalInformation": {
        },
        "personReference": {
          "identifiers": [
            {
              "type": "NOMS",
              "value": "A1212AB"
            }
          ]
        }
      }
    """.trimIndent()

    TODO() // send the message
  }

  override fun inductionScheduleUpdated(updatedInductionSchedule: InductionSchedule) {
    log.debug { "Induction updated event for prisoner [${updatedInductionSchedule.prisonNumber}]" }
    log.debug { "About to send induction schedule updated message" }
    telemetryService.trackInductionScheduleUpdated(inductionSchedule = updatedInductionSchedule)
    TODO() // send the message (similar to above)
  }
}
