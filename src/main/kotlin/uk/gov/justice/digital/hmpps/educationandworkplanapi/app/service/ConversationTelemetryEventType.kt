package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.Conversation

/**
 * An enumeration of the types of Conversation events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class ConversationTelemetryEventType(
  val value: String,
  val customDimensions: (conversation: Conversation) -> Map<String, String>,
) {
  CONVERSATION_CREATED(
    "CONVERSATION_CREATED",
    { conversation ->
      mapOf(
        "reference" to conversation.reference.toString(),
        "conversationType" to conversation.type.toString(),
        "prisonNumber" to conversation.prisonNumber,
        "prisonId" to conversation.note.createdAtPrison,
        "userId" to conversation.note.createdBy!!,
      )
    },
  ),

  CONVERSATION_UPDATED(
    "CONVERSATION_UPDATED",
    { conversation ->
      mapOf(
        "reference" to conversation.reference.toString(),
        "conversationType" to conversation.type.toString(),
        "prisonNumber" to conversation.prisonNumber,
        "prisonId" to conversation.note.lastUpdatedAtPrison,
        "userId" to conversation.note.lastUpdatedBy!!,
      )
    },
  ),
}
