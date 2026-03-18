package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.search

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PaginationMetaData

fun assertThat(actual: PaginationMetaData?) = PaginationMetaDataAssert(actual)

/**
 * AssertJ custom assertion for a single [PaginationMetaData].
 */
class PaginationMetaDataAssert(actual: PaginationMetaData?) :
  AbstractObjectAssert<PaginationMetaDataAssert, PaginationMetaData?>(
    actual,
    PaginationMetaDataAssert::class.java,
  ) {

  fun isPage(expected: Int): PaginationMetaDataAssert {
    isNotNull
    with(actual!!) {
      if (page != expected) {
        failWithMessage("Expected page to be $expected, but was $page")
      }
    }
    return this
  }

  fun hasPageSize(expected: Int): PaginationMetaDataAssert {
    isNotNull
    with(actual!!) {
      if (pageSize != expected) {
        failWithMessage("Expected pageSize to be $expected, but was $pageSize")
      }
    }
    return this
  }

  fun hasTotalPages(expected: Int): PaginationMetaDataAssert {
    isNotNull
    with(actual!!) {
      if (totalPages != expected) {
        failWithMessage("Expected totalPages to be $expected, but was $totalPages")
      }
    }
    return this
  }

  fun hasTotalElements(expected: Int): PaginationMetaDataAssert {
    isNotNull
    with(actual!!) {
      if (totalElements != expected) {
        failWithMessage("Expected totalElements to be $expected, but was $totalElements")
      }
    }
    return this
  }

  fun isFirstPage(): PaginationMetaDataAssert {
    isNotNull
    with(actual!!) {
      if (!first) {
        failWithMessage("Expected first to be true, but was $first")
      }
    }
    return this
  }

  fun isNotFirstPage(): PaginationMetaDataAssert {
    isNotNull
    with(actual!!) {
      if (first) {
        failWithMessage("Expected first to be false, but was $first")
      }
    }
    return this
  }

  fun isLastPage(): PaginationMetaDataAssert {
    isNotNull
    with(actual!!) {
      if (!last) {
        failWithMessage("Expected last to be true, but was $last")
      }
    }
    return this
  }

  fun isNotLastPage(): PaginationMetaDataAssert {
    isNotNull
    with(actual!!) {
      if (last) {
        failWithMessage("Expected last to be false, but was $last")
      }
    }
    return this
  }
}
