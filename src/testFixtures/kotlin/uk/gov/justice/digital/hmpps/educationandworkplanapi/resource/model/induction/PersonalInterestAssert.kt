package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.PersonalInterestType

fun assertThat(actual: PersonalInterest?) = PersonalInterestAssert(actual)

/**
 * AssertJ custom assertion for a single [PersonalInterest].
 */
class PersonalInterestAssert(actual: PersonalInterest?) :
  AbstractObjectAssert<PersonalInterestAssert, PersonalInterest?>(
    actual,
    PersonalInterestAssert::class.java,
  ) {

  fun hasInterestType(expected: PersonalInterestType): PersonalInterestAssert {
    isNotNull
    with(actual!!) {
      if (interestType != expected) {
        failWithMessage("Expected interestType to be $expected, but was $interestType")
      }
    }
    return this
  }

  fun hasInterestTypeOther(expected: String): PersonalInterestAssert {
    isNotNull
    with(actual!!) {
      if (interestTypeOther != expected) {
        failWithMessage("Expected interestTypeOther to be $expected, but was $interestTypeOther")
      }
    }
    return this
  }
}
