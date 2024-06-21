package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ArchiveReasonIsOtherButNoDescriptionProvided
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GoalToBeArchivedCouldNotBeFound
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.GoalToBeUnarchivedCouldNotBeFound
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.TriedToArchiveAGoalInAnInvalidState
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.TriedToUnarchiveAGoalInAnInvalidState
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.GoalReferenceMatchesReferenceInUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ArchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ErrorResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UnarchiveGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import java.util.*

private val log = KotlinLogging.logger {}

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
  fun <T> archiveGoal(
    @Valid
    @RequestBody
    archiveGoalRequest: ArchiveGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ): ResponseEntity<Any> {
    return goalService.archiveGoal(
      prisonNumber = prisonNumber,
      archiveGoalDto = goalResourceMapper.fromModelToDto(archiveGoalRequest),
    ).fold(
      {
        val status = when (it) {
          is GoalToBeArchivedCouldNotBeFound -> HttpStatus.NOT_FOUND
          is ArchiveReasonIsOtherButNoDescriptionProvided -> HttpStatus.BAD_REQUEST
          is TriedToArchiveAGoalInAnInvalidState -> HttpStatus.CONFLICT
        }
        val errorMessage = it.errorMessage()
        log.info { errorMessage }
        ResponseEntity
          .status(status)
          .body(ErrorResponse(status = status.value(), userMessage = errorMessage))
      },
      { ResponseEntity.noContent().build() },
    )
  }

  @PutMapping("{goalReference}/unarchive")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  @Transactional
  fun <T> unarchiveGoal(
    @Valid
    @RequestBody
    archiveGoalRequest: UnarchiveGoalRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
    @PathVariable goalReference: UUID,
  ): ResponseEntity<Any> {
    return goalService.unarchiveGoal(
      prisonNumber = prisonNumber,
      unarchiveGoalDto = goalResourceMapper.fromModelToDto(archiveGoalRequest),
    ).fold(
      {
        val status = when (it) {
          is GoalToBeUnarchivedCouldNotBeFound -> HttpStatus.NOT_FOUND
          is TriedToUnarchiveAGoalInAnInvalidState -> HttpStatus.CONFLICT
        }
        val errorMessage = it.errorMessage()
        log.info { errorMessage }
        ResponseEntity
          .status(status)
          .body(ErrorResponse(status = status.value(), userMessage = errorMessage))
      },
      { ResponseEntity.noContent().build() },
    )
  }
}
