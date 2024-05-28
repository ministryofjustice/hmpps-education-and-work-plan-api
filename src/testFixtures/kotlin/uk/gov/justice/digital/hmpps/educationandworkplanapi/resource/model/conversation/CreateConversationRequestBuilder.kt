package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest

fun aValidCreateReviewConversationRequest(
  prisonId: String = "BXI",
  note: String = "Pay close attention to Peter's behaviour.",
) = aValidCreateConversationRequest(prisonId, ConversationType.REVIEW, note)

fun aValidCreateConversationRequest(
  prisonId: String = "BXI",
  type: ConversationType = ConversationType.REVIEW,
  note: String = "Pay close attention to Peter's behaviour."
): CreateConversationRequest = CreateConversationRequest(
  prisonId = prisonId,
  type = type,
  note = note,
)
