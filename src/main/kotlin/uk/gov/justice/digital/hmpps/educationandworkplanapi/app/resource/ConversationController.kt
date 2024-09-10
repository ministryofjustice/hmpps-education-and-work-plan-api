package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service.ConversationService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config.DpsPrincipal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.conversation.ConversationsResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateConversationRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateConversationRequest
import java.util.UUID

@RestController
@Validated
@RequestMapping(value = ["/conversations"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ConversationController(
  private val conversationService: ConversationService,
  private val conversationMapper: ConversationsResourceMapper,
) {
  @PostMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_CONVERSATIONS)
  @Transactional
  fun createConversation(
    @Valid
    @RequestBody
    request: CreateConversationRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @AuthenticationPrincipal principal: DpsPrincipal,
  ) {
    conversationService.createConversation(
      conversationMapper.toCreateConversationDto(
        request = request,
        prisonNumber = prisonNumber,
        createdBy = principal.name,
        createdByDisplayName = principal.displayName,
      ),
    )
  }

  @PutMapping("/{prisonNumber}/{conversationReference}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_CONVERSATIONS)
  @Transactional
  fun updateConversation(
    @Valid
    @RequestBody
    request: UpdateConversationRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable conversationReference: UUID,
    @AuthenticationPrincipal principal: DpsPrincipal,
  ) = conversationService.updateConversation(
    conversationMapper.toUpdateConversationDto(
      request = request,
      reference = conversationReference,
      updatedBy = principal.name,
      updatedByDisplayName = principal.displayName,
    ),
    prisonNumber,
  )

  @GetMapping("/{prisonNumber}/{conversationReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_CONVERSATIONS)
  @Transactional
  fun getConversation(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable conversationReference: UUID,
  ) = with(conversationService.getConversation(conversationReference, prisonNumber)) {
    conversationMapper.fromDomainToModel(this)
  }

  @GetMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_CONVERSATIONS)
  @Transactional
  fun getConversations(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @RequestParam page: Int = 0,
    @RequestParam pageSize: Int = 20,
  ) = with(conversationService.getPrisonerConversations(prisonNumber, page, pageSize)) {
    conversationMapper.fromPagedDomainToModel(this)
  }
}
