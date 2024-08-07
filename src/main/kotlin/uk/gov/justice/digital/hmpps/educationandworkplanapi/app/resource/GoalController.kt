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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveGoalResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GetGoalsResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.UnarchiveGoalResult
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.exception.ReturnAnErrorException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.GoalReferenceMatchesReferenceInUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetGoalsResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UnarchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import java.util.UUID

@RestController
@Validated
@RequestMapping(value = ["/action-plans/{prisonNumber}/goals"], produces = [MediaType.APPLICATION_JSON_VALUE])
class GoalController(
  private val goalService: GoalService,
  private val goalResourceMapper: GoalResourceMapper,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  @Transactional
  fun createGoals(
    @Valid
    @RequestBody
    request: CreateGoalsRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ) {
    goalService.createGoals(
      prisonNumber = prisonNumber,
      createGoalDtos = request.goals.map { goalResourceMapper.fromModelToDto(it) },
    )
  }

  @GetMapping("{goalReference}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_AUTHORITY)
  fun getPrisonerGoal(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ): GoalResponse =
    goalResourceMapper.fromDomainToModel(goalService.getGoal(prisonNumber, goalReference))

  @PutMapping("{goalReference}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  @GoalReferenceMatchesReferenceInUpdateGoalRequest
  @Transactional
  fun updateGoal(
    @Valid
    @RequestBody
    updateGoalRequest: UpdateGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ) {
    goalService.updateGoal(
      prisonNumber = prisonNumber,
      updatedGoalDto = goalResourceMapper.fromModelToDto(updateGoalRequest),
    )
  }

  @PutMapping("{goalReference}/archive")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  @Transactional
  fun archiveGoal(
    @Valid
    @RequestBody
    archiveGoalRequest: ArchiveGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ) {
    return goalService.archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalDto = goalResourceMapper.fromModelToDto(archiveGoalRequest),
    ).let {
      when (it) {
        is ArchiveGoalResult.Success -> Unit
        is ArchiveGoalResult.GoalNotFound -> throw errorResponse(HttpStatus.NOT_FOUND, it.errorMessage())
        is ArchiveGoalResult.NoDescriptionProvidedForOther -> throw errorResponse(HttpStatus.BAD_REQUEST, it.errorMessage())
        is ArchiveGoalResult.GoalInAnInvalidState -> throw errorResponse(HttpStatus.CONFLICT, it.errorMessage())
      }
    }
  }

  @PutMapping("{goalReference}/unarchive")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  @Transactional
  fun unarchiveGoal(
    @Valid
    @RequestBody
    archiveGoalRequest: UnarchiveGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ) {
    return goalService.unarchiveGoal(
      prisonNumber = prisonNumber,
      unarchiveGoalDto = goalResourceMapper.fromModelToDto(archiveGoalRequest),
    ).let {
      when (it) {
        is UnarchiveGoalResult.Success -> Unit
        is UnarchiveGoalResult.GoalNotFound -> throw errorResponse(HttpStatus.NOT_FOUND, it.errorMessage())
        is UnarchiveGoalResult.GoalInAnInvalidState -> throw errorResponse(HttpStatus.CONFLICT, it.errorMessage())
      }
    }
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_AUTHORITY)
  fun getGoals(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @RequestParam(required = false, name = "status") statuses: Set<GoalStatus>?,
  ): GetGoalsResponse {
    return goalService.getGoals(
      GetGoalsDto(prisonNumber, statuses?.map { status -> goalResourceMapper.fromModelToDto(status) }?.toSet()),
    ).let { result ->
      when (result) {
        is GetGoalsResult.Success -> goalResourceMapper.fromDomainToModel(result)
        is GetGoalsResult.PrisonerNotFound -> throw errorResponse(HttpStatus.NOT_FOUND, result.errorMessage())
      }
    }
  }

  private fun errorResponse(status: HttpStatus, errorMessage: String): ReturnAnErrorException {
    return ReturnAnErrorException(ErrorResponse(status = status.value(), userMessage = errorMessage))
  }
}
