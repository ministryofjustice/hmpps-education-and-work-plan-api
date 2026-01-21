package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PaginationMetaData

abstract class AbstractPrisonerSearchService(
  protected val prisonerSearchApiService: PrisonerSearchApiService,
) {

  protected fun <T> List<T>.paginate(
    page: Int,
    pageSize: Int,
  ): Pair<PaginationMetaData, List<T>> {
    val totalElements = size
    val totalPages = (totalElements + pageSize - 1) / pageSize
    val paged = drop((page - 1) * pageSize).take(pageSize)

    return PaginationMetaData(
      totalElements = totalElements,
      totalPages = totalPages,
      page = page,
      pageSize = pageSize,
      first = page == 1,
      last = page == totalPages || paged.isEmpty(),
    ) to paged
  }

  protected fun isPrisonNumber(prisonerNameOrNumber: String?): Boolean {
    if (prisonerNameOrNumber.isNullOrBlank()) return false

    val prnRegex = Regex("^[A-Z]\\d{4}[A-Z]{2}$")
    return prnRegex.matches(prisonerNameOrNumber)
  }

  /**
   * Get the list of prisoners or, if a prison number is supplied,
   * only return that prisoner (if they belong to the given prison).
   */
  protected fun getPrisonerList(
    prisonerNameOrNumber: String?,
    prisonId: String,
  ): List<Prisoner> = prisonerNameOrNumber
    ?.takeIf { isPrisonNumber(it) }
    ?.let { prisonNumber ->
      prisonerSearchApiService
        .getPrisoner(prisonNumber)
        .takeIf { it.prisonId == prisonId }
        ?.let { listOf(it) }
        ?: emptyList()
    }
    ?: prisonerSearchApiService.getAllPrisonersInPrison(prisonId)
}
