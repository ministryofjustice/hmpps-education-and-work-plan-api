package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNotFoundException
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.RELEASED
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class PrisonerReleasedFromPrisonEventServiceTest {
  @InjectMocks
  private lateinit var eventService: PrisonerReleasedFromPrisonEventService

  @Mock
  private lateinit var reviewScheduleService: ReviewScheduleService

  private val objectMapper = ObjectMapper()

  @Test
  fun `should process event given reason is prisoner released`() {
    // Given
    val additionalInformation = aValidPrisonerReleasedAdditionalInformation(
      prisonNumber = "A1234BC",
      reason = RELEASED,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).exemptActiveReviewScheduleStatusDueToPrisonerRelease(
      prisonNumber = "A1234BC",
      prisonId = "BXI",
    )
  }

  @Test
  fun `should process event given reason is prisoner released due to death`() {
    // Given
    val additionalInformation = aValidPrisonerReleasedAdditionalInformation(
      prisonNumber = "A1234BC",
      reason = RELEASED,
      prisonId = "BXI",
      nomisMovementReasonCode = "DEC",
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).exemptActiveReviewScheduleStatusDueToPrisonerDeath(
      prisonNumber = "A1234BC",
      prisonId = "BXI",
    )
  }

  @Test
  fun `should process event given reason is prisoner released but prisoner does not have an active Review Schedule`() {
    // Given
    val additionalInformation = aValidPrisonerReleasedAdditionalInformation(
      prisonNumber = "A1234BC",
      reason = RELEASED,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    given(reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerRelease(any(), any()))
      .willThrow(ReviewScheduleNotFoundException("A1234BC"))

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).exemptActiveReviewScheduleStatusDueToPrisonerRelease(
      prisonNumber = "A1234BC",
      prisonId = "BXI",
    )
  }

  @Test
  fun `should process event given reason is prisoner released due to death but prisoner does not have an active Review Schedule`() {
    // Given
    val additionalInformation = aValidPrisonerReleasedAdditionalInformation(
      prisonNumber = "A1234BC",
      reason = RELEASED,
      prisonId = "BXI",
      nomisMovementReasonCode = "DEC",
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    given(reviewScheduleService.exemptActiveReviewScheduleStatusDueToPrisonerDeath(any(), any()))
      .willThrow(ReviewScheduleNotFoundException("A1234BC"))

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verify(reviewScheduleService).exemptActiveReviewScheduleStatusDueToPrisonerDeath(
      prisonNumber = "A1234BC",
      prisonId = "BXI",
    )
  }

  @ParameterizedTest
  @CsvSource(value = ["TEMPORARY_ABSENCE_RELEASE", "RELEASED_TO_HOSPITAL", "SENT_TO_COURT", "TRANSFERRED", "UNKNOWN"])
  fun `should process event but not call service given reason is not a prisoner release reason that we should process`(reason: Reason) {
    // Given
    val additionalInformation = aValidPrisonerReleasedAdditionalInformation(
      prisonNumber = "A1234BC",
      reason = reason,
      prisonId = "BXI",
    )
    val inboundEvent = anInboundEvent(additionalInformation)

    // When
    eventService.process(inboundEvent, additionalInformation)

    // Then
    verifyNoInteractions(reviewScheduleService)
  }

  private fun anInboundEvent(additionalInformation: PrisonerReleasedAdditionalInformation): InboundEvent =
    InboundEvent(
      eventType = EventType.PRISONER_RELEASED_FROM_PRISON,
      personReference = PersonReference(listOf(Identifier(type = "noms", value = "A1234BC"))),
      occurredAt = Instant.now(),
      publishedAt = Instant.now(),
      description = "Prisoner released from prison event",
      version = "1.0",
      additionalInformation = objectMapper.writeValueAsString(additionalInformation),
    )
}
