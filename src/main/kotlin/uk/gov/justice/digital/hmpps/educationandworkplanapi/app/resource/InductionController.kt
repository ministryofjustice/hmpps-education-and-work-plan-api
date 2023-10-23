package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.CreateCiagInductionRequestDataMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateCiagInductionRequest

@RestController
@RequestMapping(value = ["/ciag-inductions"], produces = [MediaType.APPLICATION_JSON_VALUE])
class InductionController(
  private val inductionService: InductionService,
  private val inductionRequestMapper: CreateCiagInductionRequestDataMapper,
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
    inductionService.createInduction(inductionRequestMapper.toCreateInductionDto(prisonNumber, request.requestDTO!!))
  }
}
