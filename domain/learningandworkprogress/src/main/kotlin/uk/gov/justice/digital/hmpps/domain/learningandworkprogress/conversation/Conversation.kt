package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation

import java.util.UUID

/**
 * Represents a single Learning And Work Progress conversation held with a Prisoner.
 *
 * The current requirements mean we have modelled the class to only contain the conversation type and the notes that
 * were taken during the conversation.
 * Data such as who was involved in the conversation (other than the prisoner), and where and when it took place are not
 * modelled as there is no current requirement for this.
 * It is anticipated that this level of detail may become a requirement, and that this class can contain those extra
 * fields at that time.
 */
data class Conversation(
  val reference: UUID,
  val prisonNumber: String,
  val type: ConversationType,
  val note: ConversationNote,
)

enum class ConversationType {
  INDUCTION,
  GENERAL,
  REVIEW,
}
