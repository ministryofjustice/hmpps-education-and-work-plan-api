package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonerSearchCriteria

@RestController
@RequestMapping("/search/prisons/{prisonId}/people")
class PrisonerSearchController(private val prisonerSearchService: PrisonerSearchService) {

  @PostMapping
  @PreAuthorize(HAS_EDIT_SESSIONS)
  fun searchPrisoners(
    @PathVariable prisonId: String,
    @RequestBody @Valid searchCriteria: PrisonerSearchCriteria,
  ): ResponseEntity<PersonSearchResult> = prisonerSearchService.searchPrisoners(prisonId, searchCriteria)
}
