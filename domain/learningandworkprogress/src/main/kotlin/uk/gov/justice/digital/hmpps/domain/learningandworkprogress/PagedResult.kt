package uk.gov.justice.digital.hmpps.domain.learningandworkprogress

/**
 * Represents a paged list of results
 *
 * In addition to a page of results, this domain model includes the total number of conversations, the total number of
 * pages, the current page number and the page size. Note, page numbers are zero-indexed.
 */
data class PagedResult<T>(
  val totalElements: Int,
  val totalPages: Int,
  val pageNumber: Int,
  val pageSize: Int,
  val content: List<T>,
)
