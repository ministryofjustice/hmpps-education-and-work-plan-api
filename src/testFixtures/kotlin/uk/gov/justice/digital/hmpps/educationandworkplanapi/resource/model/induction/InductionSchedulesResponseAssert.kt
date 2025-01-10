package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionSchedulesResponse
import java.util.function.Consumer

fun assertThat(actual: InductionSchedulesResponse?) = InductionSchedulesResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [InductionSchedulesResponse].
 */
class InductionSchedulesResponseAssert(actual: InductionSchedulesResponse?) :
  AbstractObjectAssert<InductionSchedulesResponseAssert, InductionSchedulesResponse?>(
    actual,
    InductionSchedulesResponseAssert::class.java,
  ) {

  fun hasNumberOfInductionScheduleVersions(expected: Int): InductionSchedulesResponseAssert {
    isNotNull
    with(actual!!) {
      if (inductionSchedules.size != expected) {
        failWithMessage("Expected there to be $expected InductionSchedule versions, but was ${inductionSchedules.size}")
      }
    }
    return this
  }

  fun inductionScheduleVersion(inductionScheduleVersionNumber: Int, consumer: Consumer<InductionScheduleResponseAssert>): InductionSchedulesResponseAssert {
    isNotNull
    with(actual!!) {
      val inductionSchedule = inductionSchedules.find { it.version == inductionScheduleVersionNumber }
      consumer.accept(assertThat(inductionSchedule))
    }
    return this
  }
}
