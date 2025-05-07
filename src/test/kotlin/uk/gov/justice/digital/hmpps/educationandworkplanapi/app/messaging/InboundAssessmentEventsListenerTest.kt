package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class InboundAssessmentEventsListenerTest {

  @InjectMocks
  private lateinit var inboundAssessmentEventsListener: InboundAssessmentEventsListener

  @Test
  fun `should send message to service given message is a Notification message`() {
    // Given

    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      description = null,
      who = null,
      messageAttributes = MessageAttributes(
        prisonNumber = "G0378GI",
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.now(),
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB",
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
      ),
    )

    // when
    inboundAssessmentEventsListener.onMessage(sqsMessage)
  }
}
