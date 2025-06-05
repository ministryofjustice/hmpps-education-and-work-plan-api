package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.induction.ciag.CiagInductionResponseMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CiagInductionSummaryListResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GetCiagInductionSummariesRequest

@RestController
@RequestMapping(value = ["/ciag/induction"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CiagInductionController(
  private val inductionService: InductionService,
  private val inductionResponseMapper: CiagInductionResponseMapper,
) {

  @PostMapping("/list")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(HAS_VIEW_INDUCTIONS)
  fun getInductionSummaries(
    @Valid
    @RequestBody
    request: GetCiagInductionSummariesRequest,
  ): CiagInductionSummaryListResponse = CiagInductionSummaryListResponse(listOf())
}
