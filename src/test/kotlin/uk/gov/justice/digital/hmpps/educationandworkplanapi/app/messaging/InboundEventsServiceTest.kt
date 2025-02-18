package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerMergedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerMergedAdditionalInformation.Reason.MERGE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Location.IN_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.PrisonStatus.UNDER_PRISON_CARE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReceivedAdditionalInformation.Reason.ADMISSION
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Location.OUTSIDE_PRISON
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.PrisonStatus.NOT_UNDER_PRISON_CARE
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.AdditionalInformation.PrisonerReleasedAdditionalInformation.Reason.RELEASED
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class InboundEventsServiceTest {

  @Mock
  private lateinit var objectMapper: ObjectMapper

  @Mock
  private lateinit var prisonerReceivedIntoPrisonEventService: PrisonerReceivedIntoPrisonEventService

  @Mock
  private lateinit var prisonerReleasedFromPrisonEventService: PrisonerReleasedFromPrisonEventService

  @Mock
  private lateinit var prisonerMergedEventService: PrisonerMergedEventService

  @InjectMocks
  private lateinit var inboundEventsService: InboundEventsService

  @Test
  fun `should process inbound event given prisoner received into prison event`() {
    // Given
    val inboundEvent = InboundEvent(
      eventType = EventType.PRISONER_RECEIVED_INTO_PRISON,
      description = "A prisoner has been received into prison",
      personReference = PersonReference(
        identifiers = listOf(Identifier("NOMS", "A1234BC")),
      ),
      version = "1.0",
      occurredAt = Instant.parse("2024-08-08T09:07:55+01:00"),
      publishedAt = Instant.parse("2024-08-08T09:08:55.673395103+01:00"),
      additionalInformation = "{ \"nomsNumber\": \"A6099EA\", \"reason\": \"ADMISSION\", \"details\": \"ACTIVE IN:ADM-N\", \"currentLocation\": \"IN_PRISON\", \"prisonId\": \"SWI\", \"nomisMovementReasonCode\": \"N\", \"currentPrisonStatus\": \"UNDER_PRISON_CARE\" }",
    )

    val expectedAdditionalInformation = PrisonerReceivedAdditionalInformation(
      nomsNumber = "A1234BC",
      reason = ADMISSION,
      details = "ACTIVE IN:ADM-N",
      prisonId = "SWI",
      currentLocation = IN_PRISON,
      nomisMovementReasonCode = "N",
      currentPrisonStatus = UNDER_PRISON_CARE,
    )
    given(objectMapper.readValue(any<String>(), any<Class<*>>())).willReturn(expectedAdditionalInformation)

    // When
    inboundEventsService.process(inboundEvent)

    // Then
    verify(objectMapper).readValue(
      inboundEvent.additionalInformation,
      PrisonerReceivedAdditionalInformation::class.java,
    )
    verify(prisonerReceivedIntoPrisonEventService).process(inboundEvent, expectedAdditionalInformation)
  }

  @Test
  fun `should process inbound event given prisoner released from prison event`() {
    // Given
    val inboundEvent = InboundEvent(
      eventType = EventType.PRISONER_RELEASED_FROM_PRISON,
      description = "A prisoner has been released from prison",
      personReference = PersonReference(
        identifiers = listOf(Identifier("NOMS", "A1234BC")),
      ),
      version = "1.0",
      occurredAt = Instant.parse("2024-08-08T09:07:55+01:00"),
      publishedAt = Instant.parse("2024-08-08T09:08:55.673395103+01:00"),
      additionalInformation = "{ \"nomsNumber\": \"A8101DY\", \"reason\": \"RELEASED\", \"details\": \"Movement reason code CR\", \"currentLocation\": \"OUTSIDE_PRISON\", \"prisonId\": \"MDI\", \"nomisMovementReasonCode\": \"CR\", \"currentPrisonStatus\": \"NOT_UNDER_PRISON_CARE\" }",
    )

    val expectedAdditionalInformation = PrisonerReleasedAdditionalInformation(
      nomsNumber = "A1234BC",
      reason = RELEASED,
      details = "Movement reason code CR",
      prisonId = "MDI",
      currentLocation = OUTSIDE_PRISON,
      nomisMovementReasonCode = "CR",
      currentPrisonStatus = NOT_UNDER_PRISON_CARE,
    )
    given(objectMapper.readValue(any<String>(), any<Class<*>>())).willReturn(expectedAdditionalInformation)

    // When
    inboundEventsService.process(inboundEvent)

    // Then
    verify(objectMapper).readValue(
      inboundEvent.additionalInformation,
      PrisonerReleasedAdditionalInformation::class.java,
    )
    verify(prisonerReleasedFromPrisonEventService).process(inboundEvent, expectedAdditionalInformation)
  }

  @Test
  fun `should process inbound event given prisoner merged event`() {
    // Given
    val inboundEvent = InboundEvent(
      eventType = EventType.PRISONER_MERGED,
      description = "A prisoner has been merged from A4321BC to A1234BC",
      personReference = PersonReference(
        identifiers = listOf(Identifier("NOMS", "A1234BC")),
      ),
      version = "1.0",
      occurredAt = Instant.parse("2024-08-08T09:07:55+01:00"),
      publishedAt = Instant.parse("2024-08-08T09:08:55.673395103+01:00"),
      additionalInformation = "{ \\\"nomsNumber\\\":\\\"A4321BC\\\", \\\"removedNomsNumber\\\":\\\"A4432FD\\\", \\\"reason\\\":\\\"MERGE\\\" }",
    )

    val expectedAdditionalInformation = PrisonerMergedAdditionalInformation(
      nomsNumber = "A1234BC",
      reason = MERGE,
      removedNomsNumber = "A4321BC",
    )
    given(objectMapper.readValue(any<String>(), any<Class<*>>())).willReturn(expectedAdditionalInformation)

    // When
    inboundEventsService.process(inboundEvent)

    // Then
    verify(objectMapper).readValue(
      inboundEvent.additionalInformation,
      PrisonerMergedAdditionalInformation::class.java,
    )
    verify(prisonerMergedEventService).process(inboundEvent, expectedAdditionalInformation)
  }
}
