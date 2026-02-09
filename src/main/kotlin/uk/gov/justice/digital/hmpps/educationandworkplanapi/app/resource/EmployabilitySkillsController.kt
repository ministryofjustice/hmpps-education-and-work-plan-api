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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.EmployabilitySkillsService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan.EmployabilitySkillsResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateEmployabilitySkillsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetEmployabilitySkillResponses

private val log = KotlinLogging.logger {}

@RestController
@Validated
@RequestMapping(
  value = ["/action-plans/{prisonNumber}/employability-skills"],
  produces = [MediaType.APPLICATION_JSON_VALUE],
)
class EmployabilitySkillsController(
  private val employabilitySkillsService: EmployabilitySkillsService,
  private val employabilitySkillsResourceMapper: EmployabilitySkillsResourceMapper,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_ACTIONPLANS)
  @Transactional
  fun createEmployabilitySkills(
    @Valid @RequestBody request: CreateEmployabilitySkillsRequest,
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ) {
    employabilitySkillsService.createEmployabilitySkills(
      employabilitySkillsResourceMapper.fromModelToDto(
        prisonNumber,
        request,
      ),
    )
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_ACTIONPLANS)
  fun getEmployabilitySkills(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ): GetEmployabilitySkillResponses = GetEmployabilitySkillResponses(
    employabilitySkillsService.getEmployabilitySkills(prisonNumber)
      .map { employabilitySkillsResourceMapper.fromModelToResponse(it) },
  )
}
