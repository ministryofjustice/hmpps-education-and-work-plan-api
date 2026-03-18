package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.search

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PlanStatus
import java.time.LocalDate

fun assertThat(actual: PersonResponse?) = PersonResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [PersonResponse].
 */
class PersonResponseAssert(actual: PersonResponse?) :
  AbstractObjectAssert<PersonResponseAssert, PersonResponse?>(
    actual,
    PersonResponseAssert::class.java,
  ) {

  fun hasPrisonNumber(expected: String): PersonResponseAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun hasForename(expected: String): PersonResponseAssert {
    isNotNull
    with(actual!!) {
      if (forename != expected) {
        failWithMessage("Expected forename to be $expected, but was $forename")
      }
    }
    return this
  }

  fun hasSurname(expected: String): PersonResponseAssert {
    isNotNull
    with(actual!!) {
      if (surname != expected) {
        failWithMessage("Expected surname to be $expected, but was $surname")
      }
    }
    return this
  }

  fun hasCellLocation(expected: String?): PersonResponseAssert {
    isNotNull
    with(actual!!) {
      if (cellLocation != expected) {
        failWithMessage("Expected cellLocation to be $expected, but was $cellLocation")
      }
    }
    return this
  }

  fun hasDateOfBirth(expected: LocalDate): PersonResponseAssert {
    isNotNull
    with(actual!!) {
      if (!dateOfBirth.isEqual(expected)) {
        failWithMessage("Expected dateOfBirth to be $expected, but was $dateOfBirth")
      }
    }
    return this
  }

  fun hasReleaseDate(expected: LocalDate?): PersonResponseAssert {
    isNotNull
    with(actual!!) {
      if (releaseDate?.isEqual(expected) == false) {
        failWithMessage("Expected releaseDate to be $expected, but was $releaseDate")
      }
    }
    return this
  }

  fun enteredPrisonOn(expected: LocalDate?): PersonResponseAssert {
    isNotNull
    with(actual!!) {
      if (enteredPrisonOn?.isEqual(expected) == false) {
        failWithMessage("Expected enteredPrisonOn to be $expected, but was $enteredPrisonOn")
      }
    }
    return this
  }

  fun hasPlanStatus(expected: PlanStatus): PersonResponseAssert {
    isNotNull
    with(actual!!) {
      if (planStatus != expected) {
        failWithMessage("Expected planStatus to be $expected, but was $planStatus")
      }
    }
    return this
  }
}
