package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PersonSearchRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.PrisonerSearchController.PrisonerSearchCriteria
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.Pagination
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@Service
class PrisonerSearchService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val personSearchRepository: PersonSearchRepository,
) {
  fun searchPrisoners(
    prisonId: String,
    searchCriteria: PrisonerSearchCriteria,
  ): PersonSearchResult {
    val personResponses = rawPersonData(prisonId)
    val filteredResults = applyFilters(searchCriteria, personResponses)
    val sortedResponses = applySorting(searchCriteria, filteredResults)

    // Pagination
    val page = searchCriteria.page
    val pageSize = searchCriteria.pageSize
    val totalElements = sortedResponses.size
    val totalPages = (totalElements + pageSize - 1) / pageSize
    val pagedResponses = sortedResponses.drop((page - 1) * pageSize).take(pageSize)

    return PersonSearchResult(
      Pagination(
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

  fun applyFilters(
    searchCriteria: PrisonerSearchCriteria,
    personResponses: List<PersonResponse>,
  ): List<PersonResponse> = personResponses.filter { person ->
    // Filter by prisoner name or number
    (
      searchCriteria.prisonerNameOrNumber.isNullOrBlank() ||
        person.name.contains(searchCriteria.prisonerNameOrNumber, ignoreCase = true) ||
        person.prisonNumber.equals(searchCriteria.prisonerNameOrNumber, ignoreCase = true)
      ) &&

      // Filter by hasActionPlan
      (searchCriteria.hasActionPlan == null || person.hasPlan == searchCriteria.hasActionPlan) &&

      // Filter by releaseDateBefore
      (searchCriteria.releaseDateBefore == null || person.releaseDate?.isBefore(searchCriteria.releaseDateBefore) == true) &&

      // Filter by releaseDateAfter
      (searchCriteria.releaseDateAfter == null || person.releaseDate?.isAfter(searchCriteria.releaseDateAfter) == true) &&

      // Filter by actionPlanLastUpdatedBefore
      (searchCriteria.actionPlanLastUpdatedBefore == null || person.planLastUpdated?.isBefore(searchCriteria.actionPlanLastUpdatedBefore) == true) &&

      // Filter by actionPlanLastUpdatedAfter
      (searchCriteria.actionPlanLastUpdatedAfter == null || person.planLastUpdated?.isAfter(searchCriteria.actionPlanLastUpdatedAfter) == true) &&

      // Filter by nextActionDateBefore
      (searchCriteria.nextActionDateBefore == null || person.nextActionDate?.isBefore(searchCriteria.nextActionDateBefore) == true) &&

      // Filter by nextActionDateAfter
      (searchCriteria.nextActionDateAfter == null || person.nextActionDate?.isAfter(searchCriteria.nextActionDateAfter) == true)
  }

  private fun applySorting(
    searchCriteria: PrisonerSearchCriteria,
    personResponses: List<PersonResponse>,
  ): List<PersonResponse> {
    val comparator: Comparator<PersonResponse> = when (searchCriteria.sortBy) {
      "hasPlan" -> compareBy(nullsLast()) { it.hasPlan }
      "releaseDate" -> compareBy(nullsLast()) { it.releaseDate }
      "releaseType" -> compareBy(nullsLast()) { it.releaseType }
      "cellLocation" -> compareBy(nullsLast()) { it.cellLocation }
      "nextActionDate" -> compareBy(nullsLast()) { it.nextActionDate }
      "planLastUpdated" -> compareBy(nullsLast()) { it.planLastUpdated }
      else -> compareBy(nullsLast()) { it.name }
    }

    val sortedResponses = if (searchCriteria.sortDirection.lowercase() == "desc") {
      personResponses.sortedWith(comparator.reversed())
    } else {
      personResponses.sortedWith(comparator)
    }

    return sortedResponses
  }

  private fun rawPersonData(prisonId: String): List<PersonResponse> {
    val prisonerList = prisonerSearchApiService.getAllPrisonersInPrison(prisonId)
    val prisonNumberList = prisonerList.map { it.prisonerNumber }
    val additionalDataList = personSearchRepository.additionalSearchData(prisonNumberList)

    val additionalDataListMap = additionalDataList.associateBy { it.prisonNumber }
    // create a mega list of PersonResponse
    val personResponses = prisonerList.map { prisoner ->
      with(prisoner) {
        val additionalData = additionalDataListMap[prisonerNumber]
        PersonResponse(
          prisonNumber = prisonerNumber,
          name = "$lastName, $firstName",
          dateOfBirth = dateOfBirth,
          hasPlan = additionalData?.hasActionPlan ?: false,
          cellLocation = cellLocation,
          releaseDate = releaseDate,
          releaseType = releaseType,
          nextActionDate = additionalData?.getNextActionDateAsLocalDate(),
          planLastUpdated = additionalData?.getActionPlanUpdatedAt(),
        )
      }
    }
    return personResponses
  }
}

data class PrisonerActionDto(
  val prisonNumber: String,
  val hasActionPlan: Boolean,
  val actionPlanUpdatedAt: Any?,
  val nextActionDate: Any?,
  val nextActionType: String?,
) {
  fun getNextActionDateAsLocalDate(): LocalDate? = localDate(nextActionDate)
  fun getActionPlanUpdatedAt(): LocalDate? = localDate(actionPlanUpdatedAt)

  private fun localDate(nextActionDate: Any?) = when (nextActionDate) {
    is java.sql.Date -> nextActionDate.toLocalDate() // Directly convert SQL Date
    is Date -> nextActionDate.toInstant()
      .atZone(ZoneId.systemDefault())
      .toLocalDate()
    is Instant -> nextActionDate.atZone(ZoneId.systemDefault()).toLocalDate()
    else -> null
  }
}
