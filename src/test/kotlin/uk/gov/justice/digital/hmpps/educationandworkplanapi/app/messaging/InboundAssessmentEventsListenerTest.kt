package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentEventStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.EducationAssessmentEventService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class InboundAssessmentEventsListenerTest {

  @Mock
  private lateinit var educationAssessmentEventService: EducationAssessmentEventService

  @InjectMocks
  private lateinit var listener: InboundAssessmentEventsListener

  @Test
  fun `should map SQS message to DTO and delegate to service`() {
    // Given
    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      description = null,
      who = null,
      messageAttributes = MessageAttributes(
        prisonNumber = "G0378GI",
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.of(2026, 3, 15),
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/G0378GI",
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
      ),
    )

    // When
    listener.onMessage(sqsMessage)

    // Then
    verify(educationAssessmentEventService).process(
      AssessmentEventDto(
        prisonNumber = "G0378GI",
        status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.of(2026, 3, 15),
        detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/G0378GI",
      ),
    )
  }

  @Test
  fun `should map SQS message with null detail URL to DTO`() {
    // Given
    val sqsMessage = SqsAssessmentEventMessage(
      messageId = "14e2865f-1e4b-43b0-87e8-874e7e238dd9",
      eventType = "EducationAssessmentEventCreated",
      messageAttributes = MessageAttributes(
        prisonNumber = "G0378GI",
        status = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.of(2026, 3, 15),
        detailUrl = null,
        requestId = "0650ba37-a977-4fbe-9000-4715aaecadba",
      ),
    )

    // When
    listener.onMessage(sqsMessage)

    // Then
    verify(educationAssessmentEventService).process(
      AssessmentEventDto(
        prisonNumber = "G0378GI",
        status = AssessmentEventStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
        statusChangeDate = LocalDate.of(2026, 3, 15),
        detailUrl = null,
      ),
    )
  }
}
