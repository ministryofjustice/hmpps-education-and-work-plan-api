package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.search

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PaginationMetaData
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonSearchResult
import java.util.function.Consumer

fun assertThat(actual: PersonSearchResult?) = PersonSearchResultAssert(actual)

/**
 * AssertJ custom assertion for a [PersonSearchResult].
 */
class PersonSearchResultAssert(actual: PersonSearchResult?) :
  AbstractObjectAssert<PersonSearchResultAssert, PersonSearchResult?>(
    actual,
    PersonSearchResultAssert::class.java,
  ) {

  fun hasNoPersonResponses(): PersonSearchResultAssert {
    isNotNull
    with(actual!!) {
      if (people.size > 0) {
        failWithMessage("Expected people list to be empty, but had ${people.size} elements")
      }
    }
    return this
  }

  fun hasNumberOfPersonResponses(expected: Int): PersonSearchResultAssert {
    isNotNull
    with(actual!!) {
      if (people.size != expected) {
        failWithMessage("Expected people list to contain $expected elements, but had ${people.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [PersonResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [PersonResponseAssert].
   * Returns this [PersonSearchResultAssert] to allow further chained assertions on the parent [PersonSearchResult]
   *
   * The `personResponseNumber` parameter is not zero indexed to make for better readability in tests. IE. the first
   * person should be referenced as `.personResponseNumber(1) { .... }`
   */
  fun personResponse(personResponseNumber: Int, consumer: Consumer<PersonResponseAssert>): PersonSearchResultAssert {
    isNotNull
    with(actual!!) {
      val personResponse = people[personResponseNumber - 1]
      consumer.accept(assertThat(personResponse))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [PersonResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [PersonResponseAssert].
   * Returns this [PersonSearchResultAssert] to allow further chained assertions on the parent [PersonSearchResult]
   * The assertions on all [PersonResponse]s must pass as true.
   */
  fun allPersonResponses(consumer: Consumer<PersonResponseAssert>): PersonSearchResultAssert {
    isNotNull
    with(actual!!) {
      people.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [PaginationMetaData]. Takes a lambda as the method argument
   * to call assertion methods provided by [PaginationMetaDataAssert].
   * Returns this [PersonSearchResultAssert] to allow further chained assertions on the parent [PersonSearchResult]
   */
  fun pagination(consumer: Consumer<PaginationMetaDataAssert>): PersonSearchResultAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(pagination))
    }
    return this
  }
}
