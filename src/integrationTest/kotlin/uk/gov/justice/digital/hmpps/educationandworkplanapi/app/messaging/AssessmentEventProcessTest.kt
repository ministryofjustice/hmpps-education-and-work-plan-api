package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.IntegrationTestBase
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.LocalDate

@Isolated
class AssessmentEventProcessTest : IntegrationTestBase() {
  @Test
  fun `should process assessment event message`() {
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

    // When
    sendAssessmentEvent(sqsMessage)

    // Then
    // wait until the queue is drained / message is processed
    await untilCallTo {
      assessmentEventQueueClient.countMessagesOnQueue(assessmentEventQueue.queueUrl).get()
    } matches { it == 0 }

    // TODO add asserts to check processing has been carried out once the processing code is added.
  }
}
