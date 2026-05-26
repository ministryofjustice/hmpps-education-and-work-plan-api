package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import org.assertj.core.api.AbstractObjectAssert

fun assertThat(actual: InductionScheduleEntity?) = InductionScheduleEntityAssert(actual)

/**
 * AssertJ custom assertion for [InductionScheduleEntity]
 */
class InductionScheduleEntityAssert(actual: InductionScheduleEntity?) :
  AbstractObjectAssert<InductionScheduleEntityAssert, InductionScheduleEntity?>(
    actual,
    InductionScheduleEntityAssert::class.java,
  ) {

  fun hasScheduleStatus(expected: InductionScheduleStatus): InductionScheduleEntityAssert {
    isNotNull
    with(actual!!) {
      if (scheduleStatus != expected) {
        failWithMessage("Expected scheduleStatus to be $expected, but was $scheduleStatus")
      }
    }
    return this
  }

  fun hasPrisonNumber(expected: String): InductionScheduleEntityAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }
}
