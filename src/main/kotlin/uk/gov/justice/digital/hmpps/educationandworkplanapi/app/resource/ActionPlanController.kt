package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NoteService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.ActionPlanResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.note.NoteResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ReviewScheduleAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ActionPlanSummaryListResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetActionPlanSummariesRequest

@RestController
@Validated
@RequestMapping(value = ["/action-plans"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ActionPlanController(
  private val actionPlanService: ActionPlanService,
  private val actionPlanMapper: ActionPlanResourceMapper,
  private val noteService: NoteService,
  private val noteResourceMapper: NoteResourceMapper,
  private val reviewScheduleAdapter: ReviewScheduleAdapter,
) {

  @PostMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_ACTIONPLANS)
  @Transactional
  fun createActionPlan(
    @Valid
    @RequestBody
    request: CreateActionPlanRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ) {
    actionPlanService.createActionPlan(actionPlanMapper.fromModelToDto(prisonNumber, request))
    reviewScheduleAdapter.createInitialReviewScheduleIfInductionAndActionPlanExists(prisonNumber)
  }

  @GetMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_ACTIONPLANS)
  fun getActionPlan(@PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String): ActionPlanResponse {
    val response = actionPlanMapper.fromDomainToModel(actionPlanService.getActionPlan(prisonNumber))

    // Map each goal response to its corresponding version with notes
    val goalResponsesWithNotes = response.goals.map { goalResponse ->
      val notes = noteService.getNotes(goalResponse.goalReference, EntityType.GOAL)

      // Map the notes into their respective models
      val mappedNotes = notes.map { noteResourceMapper.fromDomainToModel(it) }

      // Return a new goal response with the updated notes
      goalResponse.copy(goalNotes = mappedNotes)
    }

    // Return the response with the updated goals
    return response.copy(goals = goalResponsesWithNotes)
  }

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_ACTIONPLANS)
  fun getActionPlanSummaries(
    @Valid
    @RequestBody
    request: GetActionPlanSummariesRequest,
  ): ActionPlanSummaryListResponse =
    with(actionPlanService.getActionPlanSummaries(request.prisonNumbers)) {
      ActionPlanSummaryListResponse(actionPlanMapper.fromDomainToModel(this))
    }
}
