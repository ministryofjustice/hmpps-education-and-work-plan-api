package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource;

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service.ConversationService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation.ConversationsResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest

@RestController
@Validated
@RequestMapping(value = ["/conversations"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ConversationController(
  private val conversationService: ConversationService,
  private val conversationMapper: ConversationsResourceMapper,
  ) {
    @PostMapping("/{prisonNumber}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(HAS_EDIT_AUTHORITY)
    @Transactional
    fun createInduction(
      @Valid
      @RequestBody
      request: CreateConversationRequest,
      @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    ) {
      conversationService.createConversation(conversationMapper.toCreateConversationDto(request, prisonNumber))
    }
}
