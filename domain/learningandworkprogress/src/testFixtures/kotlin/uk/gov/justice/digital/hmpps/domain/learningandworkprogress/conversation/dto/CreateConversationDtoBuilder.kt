import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.ConversationType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.CreateConversationNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.dto.aValidCreateConversationNoteDto

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
