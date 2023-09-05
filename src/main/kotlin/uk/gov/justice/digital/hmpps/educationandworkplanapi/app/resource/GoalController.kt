package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.GoalReferenceMatchesReferenceInUpdateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.TelemetryService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateGoalRequest
import java.util.UUID

@RestController
@Validated
@RequestMapping(value = ["/action-plans/{prisonNumber}/goals"], produces = [MediaType.APPLICATION_JSON_VALUE])
class GoalController(
  private val goalService: GoalService,
  private val goalResourceMapper: GoalResourceMapper,
  private val telemetryService: TelemetryService,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  fun createGoal(
    @Valid
    @RequestBody
    request: CreateGoalRequest,
    @PathVariable prisonNumber: String,
  ) {
    goalService.createGoal(
      prisonNumber = prisonNumber,
      createGoalDto = goalResourceMapper.fromModelToDto(request),
    ).apply {
      telemetryService.trackGoalCreateEvent(this)
    }
  }

  @PutMapping("{goalReference}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  @GoalReferenceMatchesReferenceInUpdateGoalRequest
  fun updateGoal(
    @Valid
    @RequestBody
    updateGoalRequest: UpdateGoalRequest,
    @PathVariable prisonNumber: String,
    @PathVariable goalReference: UUID,
  ) {
    goalService.updateGoal(
      prisonNumber = prisonNumber,
      updatedGoalDto = goalResourceMapper.fromModelToDto(updateGoalRequest),
    )
  }
}
