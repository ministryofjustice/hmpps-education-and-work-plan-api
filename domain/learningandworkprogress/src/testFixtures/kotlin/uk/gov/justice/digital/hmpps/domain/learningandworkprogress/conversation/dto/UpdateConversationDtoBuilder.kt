package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto

import java.util.UUID

fun aValidUpdateConversationDto(
  reference: UUID = UUID.randomUUID(),
  noteContent: String = "Chris spoke positively about future work during our meeting and has made some good progress",
  prisonId: String = "BXI",
  updatedBy: String = "bjones_gen",
  updatedByDisplayName: String = "Barry Jones",
): UpdateConversationDto =
  UpdateConversationDto(
    reference = reference,
    noteContent = noteContent,
    prisonId = prisonId,
    updatedBy = updatedBy,
    updatedByDisplayName = updatedByDisplayName,
  )
