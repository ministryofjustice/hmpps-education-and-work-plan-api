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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.CiagInductionResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.CreateCiagInductionRequestMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.UpdateCiagInductionRequestMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.UpdateCiagInductionRequest

@RestController
@RequestMapping(value = ["/ciag/induction"], produces = [MediaType.APPLICATION_JSON_VALUE])
class InductionController(
  private val inductionService: InductionService,
  private val createInductionRequestMapper: CreateCiagInductionRequestMapper,
  private val updateInductionRequestMapper: UpdateCiagInductionRequestMapper,
  private val inductionResponseMapper: CiagInductionResponseMapper,
) {

  @PostMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  fun createInduction(
    @Valid
    @RequestBody
    request: CreateCiagInductionRequest,
    @PathVariable prisonNumber: String,
  ) {
    inductionService.createInduction(createInductionRequestMapper.toCreateInductionDto(prisonNumber, request))
  }

  @GetMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_AUTHORITY)
  fun getInduction(@PathVariable prisonNumber: String): CiagInductionResponse =
    with(inductionService.getInductionForPrisoner(prisonNumber)) {
      inductionResponseMapper.fromDomainToModel(this)
    }

  @PutMapping("/{prisonNumber}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(HAS_EDIT_AUTHORITY)
  fun updateInduction(
    @Valid
    @RequestBody
    request: UpdateCiagInductionRequest,
    @PathVariable prisonNumber: String,
  ) {
    inductionService.updateInduction(updateInductionRequestMapper.toUpdateInductionDto(prisonNumber, request))
  }
}
