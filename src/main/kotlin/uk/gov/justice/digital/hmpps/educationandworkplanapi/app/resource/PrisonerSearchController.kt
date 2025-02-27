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
import java.time.LocalDate

@RestController
@RequestMapping("/search/prisons/{prisonId}/people")
class PrisonerSearchController(private val prisonerSearchService: PrisonerSearchService) {

  @GetMapping
  @PreAuthorize(HAS_VIEW_INDUCTIONS)
  fun getPrisoners(
    @PathVariable prisonId: String,
    @RequestParam(required = false) prisonerNameOrNumber: String?,
    @RequestParam(required = false) hasActionPlan: Boolean?,
    @RequestParam(required = false) releaseDateBefore: LocalDate?,
    @RequestParam(required = false) releaseDateAfter: LocalDate?,
    @RequestParam(required = false) actionPlanLastUpdatedBefore: LocalDate?,
    @RequestParam(required = false) actionPlanLastUpdatedAfter: LocalDate?,
    @RequestParam(required = false) nextActionDateBefore: LocalDate?,
    @RequestParam(required = false) nextActionDateAfter: LocalDate?,
    @RequestParam(required = false, defaultValue = "prisonerName") sortBy: String,
    @RequestParam(required = false, defaultValue = "asc") sortDirection: String,
    @RequestParam(required = false, defaultValue = "1") @Min(1) page: Int,
    @RequestParam(required = false, defaultValue = "10") @Min(1) size: Int,
  ): PersonSearchResult {
    val searchCriteria = PrisonerSearchCriteria(
      prisonId,
      prisonerNameOrNumber,
      hasActionPlan,
      releaseDateBefore,
      releaseDateAfter,
      actionPlanLastUpdatedBefore,
      actionPlanLastUpdatedAfter,
      nextActionDateBefore,
      nextActionDateAfter,
      sortBy,
      sortDirection,
      page,
      size,
    )
    return prisonerSearchService.searchPrisoners(prisonId, searchCriteria)
  }

  data class PrisonerSearchCriteria(
    val prisonId: String? = null,
    val prisonerNameOrNumber: String? = null,
    val hasActionPlan: Boolean? = null,
    val releaseDateBefore: LocalDate? = null,
    val releaseDateAfter: LocalDate? = null,
    val actionPlanLastUpdatedBefore: LocalDate? = null,
    val actionPlanLastUpdatedAfter: LocalDate? = null,
    val nextActionDateBefore: LocalDate? = null,
    val nextActionDateAfter: LocalDate? = null,
    val sortBy: String = "prisonerName",
    val sortDirection: String = "asc",
    val page: Int = 1,
    val size: Int = 10,
  )
}
