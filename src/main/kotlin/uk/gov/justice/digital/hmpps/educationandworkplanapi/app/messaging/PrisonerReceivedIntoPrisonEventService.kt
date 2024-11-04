package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.CiagKpiService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.RETURN_FROM_COURT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TEMPORARY_ABSENCE_RETURN
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED

private val log = KotlinLogging.logger {}

@Service
class PrisonerReceivedIntoPrisonEventService(
  // ciagKpiService is nullable because the bean is dependent on the property `ciag-kpi-processing-rule` - when the property is not set this feature/functionality is disabled
  private val ciagKpiService: CiagKpiService?,
) {
  fun process(inboundEvent: InboundEvent, additionalInformation: PrisonerReceivedAdditionalInformation) =
    when (additionalInformation.reason) {
      ADMISSION -> {
        log.info { "Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }

        ciagKpiService?.processPrisonerAdmission(
          prisonNumber = additionalInformation.nomsNumber,
          prisonAdmittedTo = additionalInformation.prisonId,
        )
      }

      TRANSFERRED -> {
        log.info { "Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }

        ciagKpiService?.processPrisonerTransfer(
          prisonNumber = additionalInformation.nomsNumber,
          prisonTransferredTo = additionalInformation.prisonId,
        )
      }

      TEMPORARY_ABSENCE_RETURN, RETURN_FROM_COURT -> {
        log.debug { "Ignoring Processing Prisoner Received Into Prison Event with reason ${additionalInformation.reason}" }
      }
    }
}
