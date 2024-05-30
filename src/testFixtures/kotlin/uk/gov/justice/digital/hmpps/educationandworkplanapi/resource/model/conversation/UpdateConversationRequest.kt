package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateConversationRequest

fun aValidUpdateConversationRequest(
  prisonId: String = "BXI",
  note: String = "Pay close attention to Peter's behaviour.",
): UpdateConversationRequest = UpdateConversationRequest(
  prisonId = prisonId,
  note = note,
)
