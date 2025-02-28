package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.PersonSearchRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.PrisonerSearchController
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
    searchCriteria: PrisonerSearchController.PrisonerSearchCriteria,
  ): PersonSearchResult {
    val personResponses = rawPersonData(prisonId)
    // TODO - filtering on the following:
    //
    //  prisonerNameOrNumber
    //  hasActionPlan
    //  releaseDateBefore
    //  releaseDateAfter
    //  actionPlanLastUpdatedBefore
    //  actionPlanLastUpdatedAfter
    //  nextActionDateBefore
    //  nextActionDateAfter

    val sortedResponses = applySorting(searchCriteria, personResponses)

    // Pagination
    val page = searchCriteria.page
    val pageSize = searchCriteria.size
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

  private fun applySorting(
    searchCriteria: PrisonerSearchController.PrisonerSearchCriteria,
    personResponses: List<PersonResponse>,
  ): List<PersonResponse> {
    val sortedResponses = when (searchCriteria.sortBy) {
      "hasPlan" -> personResponses.sortedBy { it.hasPlan }
      "releaseDate" -> personResponses.sortedBy { it.releaseDate }
      "releaseType" -> personResponses.sortedBy { it.releaseType }
      "cellLocation" -> personResponses.sortedBy { it.cellLocation }
      "nextActionDate" -> personResponses.sortedBy { it.nextActionDate }
      "planLastUpdated" -> personResponses.sortedBy { it.planLastUpdated }
      else -> personResponses.sortedBy { it.name }
    }.let { if (searchCriteria.sortDirection.lowercase() == "desc") it.reversed() else it }
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
