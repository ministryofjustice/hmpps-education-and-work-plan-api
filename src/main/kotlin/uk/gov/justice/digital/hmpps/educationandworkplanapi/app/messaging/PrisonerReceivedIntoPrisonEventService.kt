package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleAlreadyExistsException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.InductionScheduleCalculationRule
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateInductionScheduleDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.RETURN_FROM_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import java.time.LocalDate
import java.time.ZoneId

private val log = KotlinLogging.logger {}

@Service
class PrisonerReceivedIntoPrisonEventService(private val inductionService: InductionService) {
  fun process(inboundEvent: InboundEvent, additionalInformation: PrisonerReceivedAdditionalInformation) {
    when (additionalInformation.reason) {
      ADMISSION -> {
        log.info { "Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason} and prisoner number ${inboundEvent.prisonNumber()}" }
        try {
          inductionService.createInductionSchedule(
            CreateInductionScheduleDto(
              deadlineDate = calculateDeadlineDate(inboundEvent),
              prisonNumber = inboundEvent.prisonNumber(),
              scheduleCalculationRule = InductionScheduleCalculationRule.NEW_PRISON_ADMISSION,
            ),
          )
        } catch (inductionAlreadyExistsException: InductionAlreadyExistsException) {
          log.info { "An induction already exists for prisoner with id ${inboundEvent.prisonNumber()}" }
          // update existing deadline date
          // TODO
        } catch (inductionScheduleAlreadyExistsException: InductionScheduleAlreadyExistsException) {
          log.info { "An induction schedule exists for prisoner with id ${inboundEvent.prisonNumber()}" }
        }
      }

      TRANSFERRED -> {
        log.info { "Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
        // Should already have an induction but Create induction or update existing deadline date if one exists
        // TODO - process prisoner transfer event in respect of what PLP needs to do on this event
      }

      TEMPORARY_ABSENCE_RETURN, RETURN_FROM_COURT -> {
        log.debug { "Ignoring Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
      }
    }
  }

  // This function will need to calculate the deadline date initially this will be the date the prisoner entered
  // prison plus an agreed number of days.
  private fun calculateDeadlineDate(inboundEvent: InboundEvent): LocalDate {
    val europeLondon: ZoneId = ZoneId.of("Europe/London")
    val numberOfDaysToAdd = 20
    return inboundEvent.occurredAt.atZone(europeLondon).toLocalDate().plusDays(numberOfDaysToAdd.toLong())
  }
}
