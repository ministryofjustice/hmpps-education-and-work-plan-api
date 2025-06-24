package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.AssessmentService

@RestController
@RequestMapping(value = ["/assessments"])
@PreAuthorize(HAS_VIEW_ASSESSMENTS)
class AssessmentController(
  private val assessmentService: AssessmentService,
) {

  @GetMapping("{prisonNumber}/basic-skills-assessment/required")
  fun checkEligibilityForBasicSkillsAssessment(
    @PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String,
  ): Boolean = assessmentService.requireBasicSkillsAssessment(prisonNumber)
}
