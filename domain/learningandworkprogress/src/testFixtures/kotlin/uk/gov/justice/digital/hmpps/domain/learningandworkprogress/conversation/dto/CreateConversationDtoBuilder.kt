package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto

import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationType

fun aValidCreateConversationDto(
  prisonNumber: String = aValidPrisonNumber(),
  type: ConversationType = ConversationType.REVIEW,
  note: CreateConversationNoteDto = aValidCreateConversationNoteDto(),
): CreateConversationDto =
  CreateConversationDto(
    prisonNumber = prisonNumber,
    type = type,
    note = note,
  )
