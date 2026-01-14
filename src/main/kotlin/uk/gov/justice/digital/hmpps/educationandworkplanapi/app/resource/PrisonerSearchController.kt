package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Min
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSearchService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortDirection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortField

@RestController
@RequestMapping("/search/prisons/{prisonId}/people")
class PrisonerSearchController(private val prisonerSearchService: PrisonerSearchService) {

  @GetMapping
  @PreAuthorize(HAS_SEARCH_PRISONS)
  fun getPrisoners(
    @PathVariable prisonId: String,
    @RequestParam(required = false) prisonerNameOrNumber: String?,
    @RequestParam(required = false) planStatus: PlanStatus?,
    @RequestParam(required = false) sortBy: SearchSortField = SearchSortField.PRISONER_NAME,
    @RequestParam(required = false) sortDirection: SearchSortDirection = SearchSortDirection.ASC,
    @RequestParam(required = false) @Min(1) page: Int = 1,
    @RequestParam(required = false) @Min(1) pageSize: Int = 50,
  ): PersonSearchResult {
    val searchCriteria = PrisonerSearchCriteria(
      prisonId,
      prisonerNameOrNumber,
      planStatus,
      sortBy,
      sortDirection,
      page,
      pageSize,
    )
    return prisonerSearchService.searchPrisoners(searchCriteria)
  }

  data class PrisonerSearchCriteria(
    val prisonId: String,
    val prisonerNameOrNumber: String? = null,
    val planStatus: PlanStatus? = null,
    val sortBy: SearchSortField = SearchSortField.PRISONER_NAME,
    val sortDirection: SearchSortDirection = SearchSortDirection.ASC,
    val page: Int = 1,
    val pageSize: Int = 50,
  )
}
