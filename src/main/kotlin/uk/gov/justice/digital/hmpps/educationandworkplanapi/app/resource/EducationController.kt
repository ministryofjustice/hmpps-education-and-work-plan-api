package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service.EducationService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.education.EducationResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.validator.PRISON_NUMBER_FORMAT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.EducationResponse

@RestController
@RequestMapping("/person/{prisonNumber}/education")
class EducationController(
  private val educationService: EducationService,
  private val educationResourceMapper: EducationResourceMapper,
) {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_EDUCATION)
  fun getEduction(@PathVariable @Pattern(regexp = PRISON_NUMBER_FORMAT) prisonNumber: String): EducationResponse =
    educationService.getPreviousQualificationsForPrisoner(prisonNumber).let {
      educationResourceMapper.toEducationResponse(it)
    }
}
