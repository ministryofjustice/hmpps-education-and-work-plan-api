package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import uk.gov.justice.digital.hmpps.educationandworkplanapi.randomValidPrisonNumber
import java.time.LocalDate
import java.util.UUID

fun aValidSqsAssessmentEventMessage(
  messageId: UUID = UUID.randomUUID(),
  eventType: String = "EducationAssessmentEventCreated",
  description: String? = null,
  messageAttributes: MessageAttributes = validMessageAttributes(),
  who: String? = null,
): SqsAssessmentEventMessage = SqsAssessmentEventMessage(
  messageId = messageId.toString(),
  eventType = eventType,
  description = description ?: "",
  messageAttributes = messageAttributes,
  who = who ?: "",
)

fun validMessageAttributes(
  prisonNumber: String = randomValidPrisonNumber(),
  status: EducationAssessmentStatus = EducationAssessmentStatus.ALL_RELEVANT_ASSESSMENTS_COMPLETE,
  statusChangeDate: LocalDate = LocalDate.now(),
  requestId: UUID = UUID.randomUUID(),
): MessageAttributes = MessageAttributes(
  prisonNumber = prisonNumber,
  status = status,
  statusChangeDate = statusChangeDate,
  detailUrl = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/$prisonNumber",
  requestId = requestId.toString(),
)
