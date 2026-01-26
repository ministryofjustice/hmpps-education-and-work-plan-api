package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.PrisonerSessionSearchController.PrisonerSessionSearchCriteria
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PrisonerIdsRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SearchSortDirection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSearchResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSearchResponses
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionSearchSortField
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionStatusType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionType
import java.time.LocalDate

@Service
class PrisonerSessionSearchService(
  prisonerSearchApiService: PrisonerSearchApiService,
  private val sessionSummaryService: SessionSummaryService,
  private val reviewTypeService: ReviewTypeService,
) : AbstractPrisonerSearchService(prisonerSearchApiService) {

  fun searchPrisoners(
    searchCriteria: PrisonerSessionSearchCriteria,
  ): SessionSearchResponses {
    val personResponses = getPrisonersBySearchCriteria(searchCriteria)

    val sortedAndFilteredResponses = personResponses
      .sortBy(searchCriteria)
      .filterByCriteria(searchCriteria)

    val (metadata, pagedResponses) =
      sortedAndFilteredResponses.paginate(searchCriteria.page, searchCriteria.pageSize)
    return SessionSearchResponses(pagedResponses, metadata)
  }

  private fun List<SessionSearchResponse>.filterByCriteria(
    searchCriteria: PrisonerSessionSearchCriteria,
  ): List<SessionSearchResponse> = this.filter { prisoner ->

    val matchesNameOrNumber =
      searchCriteria.prisonerNameOrNumber.isNullOrBlank() ||
        prisoner.forename.contains(searchCriteria.prisonerNameOrNumber, ignoreCase = true) ||
        prisoner.surname.contains(searchCriteria.prisonerNameOrNumber, ignoreCase = true) ||
        prisoner.prisonNumber.equals(searchCriteria.prisonerNameOrNumber, ignoreCase = true)

    val matchesSessionType =
      searchCriteria.sessionType == null ||
        prisoner.sessionType == searchCriteria.sessionType

    matchesNameOrNumber && matchesSessionType
  }

  private fun List<SessionSearchResponse>.sortBy(searchCriteria: PrisonerSessionSearchCriteria): List<SessionSearchResponse> {
    val comparator: Comparator<SessionSearchResponse> = when (searchCriteria.sortBy) {
      SessionSearchSortField.PRISONER_NAME -> compareBy { it.surname }
      SessionSearchSortField.PRISON_NUMBER -> compareBy { it.prisonNumber }
      SessionSearchSortField.RELEASE_DATE -> compareBy(nullsLast()) { it.releaseDate }
      SessionSearchSortField.CELL_LOCATION -> compareBy(nullsLast()) { it.cellLocation }
      SessionSearchSortField.SESSION_TYPE -> compareBy { person -> customSessionTypeOrder[person.sessionType] }
      SessionSearchSortField.DUE_BY -> compareBy { it.deadlineDate }
      SessionSearchSortField.EXEMPTION_DATE -> compareBy { it.exemptionDate }
      SessionSearchSortField.EXEMPTION_REASON -> compareBy { it.exemptionReason }
    }

    return when (searchCriteria.sortDirection) {
      SearchSortDirection.ASC -> this.sortedWith(comparator)
      SearchSortDirection.DESC -> this.sortedWith(comparator.reversed())
    }
  }

  val customSessionTypeOrder = listOf(
    SessionType.PRE_RELEASE_REVIEW,
    SessionType.REVIEW,
    SessionType.TRANSFER_REVIEW,
    SessionType.INDUCTION,
  ).withIndex().associate { it.value to it.index }

  private fun getPrisonersBySearchCriteria(
    searchCriteria: PrisonerSessionSearchCriteria,
  ): List<SessionSearchResponse> {
    val prisonerList =
      getPrisonerList(searchCriteria.prisonerNameOrNumber, searchCriteria.prisonId)

    val prisonNumberList = prisonerList.map { it.prisonerNumber }
    val additionalDataList = getSessions(searchCriteria.sessionStatusType, prisonNumberList)

    val additionalDataByPrisonNumber = additionalDataList.associateBy { it.prisonNumber }

    return prisonerList.mapNotNull { prisoner ->
      val additionalData = additionalDataByPrisonNumber[prisoner.prisonerNumber]
        ?: return@mapNotNull null // skip this prisoner if there is no additional data

      SessionSearchResponse(
        prisonNumber = prisoner.prisonerNumber,
        forename = prisoner.firstName,
        surname = prisoner.lastName,
        dateOfBirth = prisoner.dateOfBirth,
        cellLocation = prisoner.cellLocation!!,
        releaseDate = prisoner.releaseDate,
        sessionType = getSessionType(
          additionalData.sessionType,
          additionalData.scheduleCalculationRule,
          prisoner.releaseDate,
        ),
        deadlineDate = additionalData.deadlineDate,
        exemptionReason = additionalData.exemptionReason,
        exemptionDate = additionalData.exemptionDate,
      )
    }
  }

  private fun getSessionType(
    sessionType: SessionResponse.SessionType,
    scheduleCalculationRule: String,
    releaseDate: LocalDate?,
  ): SessionType {
    if (sessionType == SessionResponse.SessionType.REVIEW) {
      return reviewTypeService.mapToSessionType(reviewTypeService.reviewType(releaseDate, scheduleCalculationRule))
    }

    return SessionType.INDUCTION
  }

  private fun getSessions(
    sessionStatusType: SessionStatusType,
    prisonNumberList: List<String>,
  ): List<SessionResponse> {
    val sessionData =
      sessionSummaryService.getSessions(status = sessionStatusType, requestIds = PrisonerIdsRequest(prisonNumberList))

    return sessionData.sessions
  }
}
