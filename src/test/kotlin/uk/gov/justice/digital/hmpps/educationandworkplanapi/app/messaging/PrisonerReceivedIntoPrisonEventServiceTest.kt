package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.CiagKpiService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.TRANSFERRED
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class PrisonerReceivedIntoPrisonEventServiceTest {
  @InjectMocks
  private lateinit var eventService: PrisonerReceivedIntoPrisonEventService

  @Mock
  private lateinit var ciagKpiService: CiagKpiService

  @Mock
  private lateinit var reviewScheduleService: ReviewScheduleService

  private val objectMapper = ObjectMapper()

  @Test
  fun `should process event given reason is prisoner admission`() {
    // Given
    val eventOccurredAt = Instant.now()
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = "A1234BC",
      reason = ADMISSION,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(
      additionalInformation = additionalInformation,
      eventOccurredAt = eventOccurredAt,
    )

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(ciagKpiService).processPrisonerAdmission("A1234BC", "BXI", eventOccurredAt)
    verifyNoInteractions(reviewScheduleService)
  }

  @Test
  fun `should process event given reason is prisoner transfer`() {
    // Given
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(
      prisonNumber = "A1234BC",
      reason = TRANSFERRED,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).exemptAndReScheduleActiveReviewScheduleStatusDueToPrisonerTransfer("A1234BC", "BXI")
  }

  @ParameterizedTest
  @CsvSource(value = ["TEMPORARY_ABSENCE_RETURN", "RETURN_FROM_COURT"])
  fun `should process event but not call service given reason is not a prisoner admission reason that we should process`(reason: Reason) {
    // Given
    val additionalInformation = aValidPrisonerReceivedAdditionalInformation(reason = reason)
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verifyNoInteractions(ciagKpiService)
    verifyNoInteractions(reviewScheduleService)
  }

  private fun anInboundEvent(
    additionalInformation: PrisonerReceivedAdditionalInformation,
    eventOccurredAt: Instant = Instant.now(),
  ): InboundEvent =
    InboundEvent(
      eventType = EventType.PRISONER_RECEIVED_INTO_PRISON,
      personReference = PersonReference(listOf(Identifier(type = "noms", value = "A1234BC"))),
      occurredAt = eventOccurredAt,
      publishedAt = Instant.now(),
      description = "Prisoner received into prison event",
      version = "1.0",
      additionalInformation = objectMapper.writeValueAsString(additionalInformation),
    )
}
