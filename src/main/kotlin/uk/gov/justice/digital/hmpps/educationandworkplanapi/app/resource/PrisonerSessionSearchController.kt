package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

import jakarta.validation.constraints.Min
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service.PrisonerSessionSearchService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortDirection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSearchResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSearchSortField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionStatusType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionType

@RestController
@RequestMapping("/session/prisons/{prisonId}/search")
class PrisonerSessionSearchController(private val prisonerSessionSearchService: PrisonerSessionSearchService) {

  @GetMapping
  @PreAuthorize(HAS_SEARCH_PRISONS)
  fun getPrisoners(
    @PathVariable prisonId: String,
    @RequestParam(required = false) prisonerNameOrNumber: String?,
    @RequestParam(required = false) sessionType: SessionType?,
    @RequestParam(required = false) sessionStatusType: SessionStatusType = SessionStatusType.DUE,
    @RequestParam(required = false) sortBy: SessionSearchSortField = SessionSearchSortField.PRISONER_NAME,
    @RequestParam(required = false) sortDirection: SearchSortDirection = SearchSortDirection.ASC,
    @RequestParam(required = false) @Min(1) page: Int = 1,
    @RequestParam(required = false) @Min(1) pageSize: Int = 50,
  ): SessionSearchResponses {
    val searchCriteria = PrisonerSessionSearchCriteria(
      prisonId,
      prisonerNameOrNumber,
      sessionType,
      sessionStatusType,
      sortBy,
      sortDirection,
      page,
      pageSize,
    )

    return prisonerSessionSearchService.searchPrisoners(searchCriteria)
  }

  data class PrisonerSessionSearchCriteria(
    val prisonId: String,
    val prisonerNameOrNumber: String? = null,
    val sessionType: SessionType?,
    val sessionStatusType: SessionStatusType,
    val sortBy: SessionSearchSortField = SessionSearchSortField.PRISONER_NAME,
    val sortDirection: SearchSortDirection = SearchSortDirection.ASC,
    val page: Int = 1,
    val pageSize: Int = 50,
  )
}
