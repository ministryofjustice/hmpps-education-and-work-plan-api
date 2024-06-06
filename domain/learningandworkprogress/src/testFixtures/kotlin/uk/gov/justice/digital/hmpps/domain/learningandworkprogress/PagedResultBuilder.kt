package uk.gov.justice.digital.hmpps.domain.learningandworkprogress

fun <T> aValidPagedResult(
  content: List<T> = emptyList(),
  page: Int = 0,
  pageSize: Int = 20,
): PagedResult<T> =
  PagedResult(
    totalElements = content.size,
    totalPages = (content.size / pageSize) + 1,
    pageNumber = page,
    pageSize = pageSize,
    content = content.chunked(pageSize).getOrNull(page) ?: emptyList(),
  )
