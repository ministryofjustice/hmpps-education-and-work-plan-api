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
import org.mockito.kotlin.verifyNoInteractions
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class InboundEventsListenerTest {

  @Mock
  private lateinit var objectMapper: ObjectMapper

  @Mock
  private lateinit var inboundEventsService: InboundEventsService

  @InjectMocks
  private lateinit var inboundEventsListener: InboundEventsListener

  @Test
  fun `should send message to service given message is a Notification message`() {
    // Given
    val sqsMessage = SqsMessage(
      Type = "Notification",
      Message = """
        {
          "eventType": "prison-offender-events.prisoner.received",
          "personReference": { "identifiers": [ { "type": "NOMS", "value": "A1234BC" } ] },
          "occurredAt": "2024-08-08T09:07:55+01:00",
          "publishedAt": "2024-08-08T09:08:55.673395103+01:00",
          "description": "A prisoner has been received into prison",
          "version": "1.0",
          "additionalInformation": { "nomsNumber": "A6099EA", "reason": "ADMISSION", "details": "ACTIVE IN:ADM-N", "currentLocation": "IN_PRISON", "prisonId": "SWI", "nomisMovementReasonCode": "N", "currentPrisonStatus": "UNDER_PRISON_CARE" }
        }        
      """.trimIndent(),
      MessageId = UUID.randomUUID(),
    )

    val expectedInboundEvent = InboundEvent(
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
    given(objectMapper.readValue(any<String>(), any<Class<*>>())).willReturn(expectedInboundEvent)

    // When
    inboundEventsListener.onMessage(sqsMessage)

    // Then
    verify(objectMapper).readValue(sqsMessage.Message, InboundEvent::class.java)
    verify(inboundEventsService).process(expectedInboundEvent)
  }

  @Test
  fun `should not send message to service given message is not a Notification message`() {
    // Given
    val sqsMessage = SqsMessage(
      Type = "some-other-message-type",
      Message = "some message content",
      MessageId = UUID.randomUUID(),
    )

    // When
    inboundEventsListener.onMessage(sqsMessage)

    // Then
    verifyNoInteractions(objectMapper)
    verifyNoInteractions(inboundEventsService)
  }
}
