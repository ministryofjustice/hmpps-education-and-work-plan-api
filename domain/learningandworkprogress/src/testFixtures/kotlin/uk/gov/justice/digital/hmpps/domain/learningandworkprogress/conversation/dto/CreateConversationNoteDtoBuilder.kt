package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto

fun aValidCreateConversationNoteDto(
  prisonId: String = "BXI",
  content: String = "Chris engaged well during our meeting and has made some good progress",
): CreateConversationNoteDto =
  CreateConversationNoteDto(
    prisonId = prisonId,
    content = content,
  )
