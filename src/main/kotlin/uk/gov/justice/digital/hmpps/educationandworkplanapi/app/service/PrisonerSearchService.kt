package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PersonSearchRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.PrisonerSearchController.PrisonerSearchCriteria
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortDirection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortField

@Service
class PrisonerSearchService(
  prisonerSearchApiService: PrisonerSearchApiService,
  private val personSearchRepository: PersonSearchRepository,
) : AbstractPrisonerSearchService(prisonerSearchApiService) {

  // TODO (OOM mitigation follow-up): this endpoint paginates entirely in memory. It loads the WHOLE
  //  prison roster (see getPrisonersBySearchCriteria -> getAllPrisonersInPrison, pageSize=9999), builds
  //  a full List<PersonResponse>, then sorts/filters/paginates in memory and discards all but one page.
  //  For a large prison that's a heavy per-request allocation that drives GC pressure under load — a
  //  suspected contributor to the June 2026 OOM incident, though reproduction was inconclusive so the
  //  exact root cause remains unconfirmed. Worth fixing regardless: push filtering, sorting and
  //  pagination down to the data sources so only one page (~pageSize) is ever held in memory. Non-trivial
  //  because the result spans two sources (prisoner-search-api for the roster + the local DB for
  //  planStatus via PersonSearchRepository.additionalSearchData), so the sort/filter fields straddle
  //  both. Options to explore: have prisoner-search do name/number filtering + paging, and/or maintain
  //  the searchable fields locally so a single paged DB query can serve it.
  fun searchPrisoners(
    searchCriteria: PrisonerSearchCriteria,
  ): PersonSearchResult {
    val personResponses = getPrisonersBySearchCriteria(searchCriteria)

    val sortedAndFilteredResponses = personResponses
      .sortBy(searchCriteria)
      .filterByCriteria(searchCriteria)

    val (metadata, pagedResponses) =
      sortedAndFilteredResponses.paginate(searchCriteria.page, searchCriteria.pageSize)
    return PersonSearchResult(metadata, pagedResponses)
  }

  private fun List<PersonResponse>.filterByCriteria(searchCriteria: PrisonerSearchCriteria): List<PersonResponse> = this.filter { prisoner ->
    // Filter by prisoner name or number
    (
      searchCriteria.prisonerNameOrNumber.isNullOrBlank() ||
        prisoner.forename.contains(searchCriteria.prisonerNameOrNumber, ignoreCase = true) ||
        prisoner.surname.contains(searchCriteria.prisonerNameOrNumber, ignoreCase = true) ||
        prisoner.prisonNumber.equals(searchCriteria.prisonerNameOrNumber, ignoreCase = true)
      ) &&

      // Filter by hasActionPlan
      (searchCriteria.planStatus == null || prisoner.planStatus == searchCriteria.planStatus)
  }

  private fun List<PersonResponse>.sortBy(searchCriteria: PrisonerSearchCriteria): List<PersonResponse> {
    val comparator: Comparator<PersonResponse> = when (searchCriteria.sortBy) {
      SearchSortField.PRISONER_NAME -> compareBy { it.surname }
      SearchSortField.PRISON_NUMBER -> compareBy { it.prisonNumber }
      SearchSortField.RELEASE_DATE -> compareBy(nullsLast()) { it.releaseDate }
      SearchSortField.ENTERED_PRISON_DATE -> compareBy(nullsLast()) { it.enteredPrisonOn }
      SearchSortField.CELL_LOCATION -> compareBy(nullsLast()) { it.cellLocation }
      SearchSortField.PLAN_STATUS -> compareBy { person -> customPlanStatusOrder[person.planStatus] }
    }

    return when (searchCriteria.sortDirection) {
      SearchSortDirection.ASC -> this.sortedWith(comparator)
      SearchSortDirection.DESC -> this.sortedWith(comparator.reversed())
    }
  }

  val customPlanStatusOrder = listOf(
    PlanStatus.ACTIVE_PLAN,
    PlanStatus.NEEDS_PLAN,
    PlanStatus.EXEMPT,
  ).withIndex().associate { it.value to it.index }

  private fun getPrisonersBySearchCriteria(searchCriteria: PrisonerSearchCriteria): List<PersonResponse> {
    // If the user supplied a prison number, only search for that prisoner
    val prisonerList =
      getPrisonerList(searchCriteria.prisonerNameOrNumber, searchCriteria.prisonId)

    val prisonNumberList = prisonerList.map { it.prisonerNumber }
    val additionalDataList = personSearchRepository.additionalSearchData(prisonNumberList)

    val additionalDataListMap = additionalDataList.associateBy { it.prisonNumber }
    // create a mega list of PersonResponse
    val personResponses = prisonerList.map { prisoner ->
      with(prisoner) {
        val additionalData = additionalDataListMap[prisonerNumber]
        PersonResponse(
          prisonNumber = prisonerNumber,
          forename = firstName,
          surname = lastName,
          dateOfBirth = dateOfBirth,
          cellLocation = cellLocation,
          releaseDate = releaseDate,
          enteredPrisonOn = receptionDate,
          planStatus = mapStatus(additionalData?.planStatus),
        )
      }
    }
    return personResponses
  }

  private fun mapStatus(planStatus: String?): PlanStatus = when (planStatus) {
    "PENDING_SCREENING_AND_ASSESSMENTS" -> PlanStatus.PENDING_SCREENING_AND_ASSESSMENTS
    "ACTIVE_PLAN" -> PlanStatus.ACTIVE_PLAN
    "EXEMPT" -> PlanStatus.EXEMPT
    else -> PlanStatus.NEEDS_PLAN
  }
}

data class AdditionalPrisonerDataDto(
  val prisonNumber: String,
  val planStatus: String,
)
