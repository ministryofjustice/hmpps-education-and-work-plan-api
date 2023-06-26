package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.GoalResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest

@RestController
@RequestMapping(value = ["/action-plans/{prisonNumber}/goals"], produces = [MediaType.APPLICATION_JSON_VALUE])
class GoalController(
  private val goalService: GoalService,
  private val goalResourceMapper: GoalResourceMapper,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  fun createGoal(@PathVariable prisonNumber: String, @RequestBody request: CreateGoalRequest) {
    goalService.createGoal(
      prisonNumber = prisonNumber,
      goal = goalResourceMapper.fromModelToDomain(request),
    )
  }
}
