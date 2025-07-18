package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
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
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.CreateNoteDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.EntityType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto.NoteType
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NoteService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewScheduleNoReleaseDateForSentenceTypeException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.GoalReferenceMatchesReferenceInUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.ScheduleAdapter
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CompleteGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetGoalsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UnarchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import java.util.UUID

private val log = KotlinLogging.logger {}

@RestController
@Validated
@RequestMapping(value = ["/action-plans/{prisonNumber}/goals"], produces = [MediaType.APPLICATION_JSON_VALUE])
class GoalController(
  private val goalService: GoalService,
  private val goalResourceMapper: GoalResourceMapper,
  private val noteService: NoteService,
  private val scheduleAdapter: ScheduleAdapter,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_GOALS)
  @Transactional
  fun createGoals(
    @Valid @RequestBody request: CreateGoalsRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ) {
    goalService.createGoals(
      prisonNumber = prisonNumber,
      createGoalDtos = request.goals.map { goalResourceMapper.fromModelToDto(it) },
    )
    try {
      scheduleAdapter.completeInductionScheduleAndCreateInitialReviewSchedule(prisonNumber)
    } catch (e: ReviewScheduleNoReleaseDateForSentenceTypeException) {
      log.warn { "Exception thrown when completing induction or creating review schedule: ${e.message}" }
    }
  }

  @GetMapping("{goalReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_GOALS)
  fun getPrisonerGoal(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ): GoalResponse {
    val goal = goalService.getGoal(prisonNumber, goalReference)
    val goalNotes = noteService.getNotes(goal.reference, EntityType.GOAL)
    return goalResourceMapper.fromDomainToModel(goal, goalNotes)
  }

  @PutMapping("{goalReference}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_GOALS)
  @GoalReferenceMatchesReferenceInUpdateGoalRequest
  @Transactional
  fun updateGoal(
    @Valid
    @RequestBody
    updateGoalRequest: UpdateGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ) {
    // Update the goal
    goalService.updateGoal(
      prisonNumber = prisonNumber,
      updatedGoalDto = goalResourceMapper.fromModelToDto(updateGoalRequest),
    )
  }

  @PutMapping("{goalReference}/archive")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_GOALS)
  @Transactional
  fun archiveGoal(
    @Valid @RequestBody archiveGoalRequest: ArchiveGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ) {
    val goal = goalService.archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalDto = goalResourceMapper.fromModelToDto(archiveGoalRequest),
    )
    createGoalNote(prisonNumber, goal, archiveGoalRequest.note, NoteType.GOAL_ARCHIVAL)
  }

  @PutMapping("{goalReference}/complete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_GOALS)
  @Transactional
  fun completeGoal(
    @Valid @RequestBody completeGoalRequest: CompleteGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ) {
    val goal = goalService.completeGoal(
      prisonNumber = prisonNumber,
      completeGoalDto = goalResourceMapper.fromModelToDto(completeGoalRequest),
    )
    createGoalNote(prisonNumber, goal, completeGoalRequest.note, NoteType.GOAL_COMPLETION)
  }

  private fun createGoalNote(prisonNumber: String, goal: Goal, noteText: String?, noteType: NoteType) {
    if (!noteText.isNullOrEmpty()) {
      noteService.createNote(
        CreateNoteDto(
          prisonNumber = prisonNumber,
          entityReference = goal.reference,
          entityType = EntityType.GOAL,
          noteType = noteType,
          content = noteText,
          createdAtPrison = goal.createdAtPrison,
          lastUpdatedAtPrison = goal.lastUpdatedAtPrison,
        ),
      )
    }
  }

  @PutMapping("{goalReference}/unarchive")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_GOALS)
  @Transactional
  fun unarchiveGoal(
    @Valid @RequestBody archiveGoalRequest: UnarchiveGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ) {
    goalService.unarchiveGoal(
      prisonNumber = prisonNumber,
      unarchiveGoalDto = goalResourceMapper.fromModelToDto(archiveGoalRequest),
    )

    // delete any goal notes
    noteService.deleteNote(archiveGoalRequest.goalReference, EntityType.GOAL, NoteType.GOAL_ARCHIVAL)
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_GOALS)
  fun getGoals(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @RequestParam(required = false, name = "status") statuses: Set<GoalStatus>?,
  ): GetGoalsResponse {
    val goals = goalService.getGoals(GetGoalsDto(prisonNumber, convertStatuses(statuses)))

    return GetGoalsResponse(
      goals = goals.map {
        val goalNotes = noteService.getNotes(it.reference, EntityType.GOAL)
        goalResourceMapper.fromDomainToModel(it, goalNotes)
      },
    )
  }

  // convert from the generated enum to the one we use in persistence
  private fun convertStatuses(statuses: Set<GoalStatus>?) = statuses?.map { status -> goalResourceMapper.toGoalStatus(status) }?.toSet()
}
