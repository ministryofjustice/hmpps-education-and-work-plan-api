package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationsResponse

fun assertThat(actual: ConversationsResponse?) = ConversationsResponseAssert(actual)

/**
 * AssertJ custom assertion for [ConversationsResponse]
 */
class ConversationsResponseAssert(actual: ConversationsResponse?) :
  AbstractObjectAssert<ConversationsResponseAssert, ConversationsResponse?>(actual, ConversationsResponseAssert::class.java) {

  fun hasPageNumber(expected: Int): ConversationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (pageNumber != expected) {
        failWithMessage("Expected pageNumber to be $expected, but was $pageNumber")
      }
    }
    return this
  }

  fun hasPageSize(expected: Int): ConversationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (pageSize != expected) {
        failWithMessage("Expected pageSize to be $expected, but was $pageSize")
      }
    }
    return this
  }

  fun hasTotalPages(expected: Int): ConversationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (totalPages != expected) {
        failWithMessage("Expected totalPages to be $expected, but was $totalPages")
      }
    }
    return this
  }

  fun hasTotalElements(expected: Int): ConversationsResponseAssert {
    isNotNull
    with(actual!!) {
      if (totalElements != expected) {
        failWithMessage("Expected totalElements to be $expected, but was $totalElements")
      }
    }
    return this
  }

  fun contentHasSize(expected: Int): ConversationsResponseAssert {
    isNotNull
    with(actual!!) {
      assertThat(content).hasSize(expected)
    }
    return this
  }
}
