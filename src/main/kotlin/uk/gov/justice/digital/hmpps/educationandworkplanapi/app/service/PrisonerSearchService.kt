package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PersonSearchRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.PrisonerSearchController.PrisonerSearchCriteria
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PaginationMetaData
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortDirection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortField

@Service
class PrisonerSearchService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val personSearchRepository: PersonSearchRepository,
) {

  private fun isPrisonNumber(prisonerNameOrNumber: String?): Boolean {
    if (prisonerNameOrNumber.isNullOrBlank()) return false

    val prnRegex = Regex("^[A-Z]\\d{4}[A-Z]{2}$")
    return prnRegex.matches(prisonerNameOrNumber)
  }

  fun searchPrisoners(
    searchCriteria: PrisonerSearchCriteria,
  ): PersonSearchResult {
    val personResponses = rawPersonData(searchCriteria)

    val sortedAndFilteredResponses = personResponses
      .sortBy(searchCriteria)
      .filterByCriteria(searchCriteria)

    // Pagination
    val page = searchCriteria.page
    val pageSize = searchCriteria.pageSize
    val totalElements = sortedAndFilteredResponses.size
    val totalPages = (totalElements + pageSize - 1) / pageSize
    val pagedResponses = sortedAndFilteredResponses.drop((page - 1) * pageSize).take(pageSize)

    return PersonSearchResult(
      PaginationMetaData(
        totalElements = totalElements,
        totalPages = totalPages,
        page = page,
        pageSize = pageSize,
        first = page == 1,
        last = page == totalPages || pagedResponses.isEmpty(),
      ),
      pagedResponses,
    )
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

  private fun applySorting(
    searchCriteria: PrisonerSearchCriteria,
    personResponses: List<PersonResponse>,
  ): List<PersonResponse> {
    val comparator: Comparator<PersonResponse> = when (searchCriteria.sortBy.name) {
      "planStatus" -> compareBy(nullsLast()) { it.planStatus }
      "releaseDate" -> compareBy(nullsLast()) { it.releaseDate }
      "enteredPrisonOn" -> compareBy(nullsLast()) { it.enteredPrisonOn }
      "cellLocation" -> compareBy(nullsLast()) { it.cellLocation }
      else -> compareBy(nullsLast()) { it.surname }
    }

    val sortedResponses = if (searchCriteria.sortDirection.name.lowercase() == "desc") {
      personResponses.sortedWith(comparator.reversed())
    } else {
      personResponses.sortedWith(comparator)
    }

    return sortedResponses
  }

  private fun rawPersonData(searchCriteria: PrisonerSearchCriteria): List<PersonResponse> {
    // If the user supplied a prison number, only search for that prisoner
    val prisonerList =
      searchCriteria.prisonerNameOrNumber
        ?.takeIf { isPrisonNumber(it) }
        ?.let { prisonNumber ->
          prisonerSearchApiService
            .getPrisoner(prisonNumber)
            .takeIf { it.prisonId == searchCriteria.prisonId }
            ?.let { listOf(it) }
            ?: emptyList()
        }
        ?: prisonerSearchApiService.getAllPrisonersInPrison(searchCriteria.prisonId)

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
    "ACTIVE_PLAN" -> PlanStatus.ACTIVE_PLAN
    "EXEMPT" -> PlanStatus.EXEMPT
    else -> PlanStatus.NEEDS_PLAN
  }
}

data class AdditionalPrisonerDataDto(
  val prisonNumber: String,
  val planStatus: String,
)
