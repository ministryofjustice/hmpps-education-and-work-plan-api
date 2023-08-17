package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.ActionPlanResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest

@RestController
@RequestMapping(value = ["/action-plans/{prisonNumber}"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ActionPlanController(
  private val actionPlanService: ActionPlanService,
  private val actionPlanMapper: ActionPlanResourceMapper,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  fun createActionPlan(
    @Valid
    @RequestBody
    request: CreateActionPlanRequest,
    @PathVariable prisonNumber: String,
  ) {
    actionPlanService.createActionPlan(actionPlanMapper.fromModelToDomain(prisonNumber, request))
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_AUTHORITY)
  fun getActionPlan(@PathVariable prisonNumber: String): ActionPlanResponse =
    with(actionPlanService.getActionPlan(prisonNumber)) {
      actionPlanMapper.fromDomainToModel(this)
    }
}
