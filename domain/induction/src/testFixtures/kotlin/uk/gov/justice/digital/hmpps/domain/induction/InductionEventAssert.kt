package uk.gov.justice.digital.hmpps.domain.induction

import org.assertj.core.api.AbstractObjectAssert

fun assertThat(actual: Induction?) = InductionAssert(actual)

/**
 * AssertJ custom assertion for [Induction].
 */
class InductionAssert(actual: Induction?) :
  AbstractObjectAssert<InductionAssert, Induction?>(actual, InductionAssert::class.java) {

  fun hasPrisonNumber(expected: String): InductionAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }
}
