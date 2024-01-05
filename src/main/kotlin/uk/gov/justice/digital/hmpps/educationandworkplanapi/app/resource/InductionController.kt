package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.InductionResourceMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateInductionRequest

@RestController
@RequestMapping(value = ["/inductions"], produces = [MediaType.APPLICATION_JSON_VALUE])
class InductionController(
  private val inductionService: InductionService,
  private val inductionMapper: InductionResourceMapper,
) {

  @PostMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  fun createInduction(
    @Valid
    @RequestBody
    request: CreateInductionRequest,
    @PathVariable prisonNumber: String,
  ) {
    inductionService.createInduction(inductionMapper.toCreateInductionDto(prisonNumber, request))
  }

  @GetMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_AUTHORITY)
  fun getInduction(@PathVariable prisonNumber: String): InductionResponse =
    with(inductionService.getInductionForPrisoner(prisonNumber)) {
      inductionMapper.toInductionResponse(this)
    }

  @PutMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  fun updateInduction(
    @Valid
    @RequestBody
    request: UpdateInductionRequest,
    @PathVariable prisonNumber: String,
  ) {
    inductionService.updateInduction(inductionMapper.toUpdateInductionDto(prisonNumber, request))
  }
}
