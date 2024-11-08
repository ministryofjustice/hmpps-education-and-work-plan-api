package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleCalculationRule as InductionScheduleCalculationRuleResponse

fun assertThat(actual: InductionScheduleResponse?) = InductionScheduleResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [InductionScheduleResponse].
 */
class InductionScheduleResponseAssert(actual: InductionScheduleResponse?) :
  AbstractObjectAssert<InductionScheduleResponseAssert, InductionScheduleResponse?>(
    actual,
    InductionScheduleResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAfter(dateTime: OffsetDateTime): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (!createdAt.isAfter(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAfter(dateTime: OffsetDateTime): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (!updatedAt.isAfter(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedByDisplayName(expected: String): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasScheduleCalculationRule(expected: InductionScheduleCalculationRuleResponse): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (scheduleCalculationRule != expected) {
        failWithMessage("Expected scheduleCalculationRule to be $expected, but was $scheduleCalculationRule")
      }
    }
    return this
  }

  fun wasStatus(expected: InductionScheduleStatus): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (scheduleStatus != expected) {
        failWithMessage("Expected scheduleStatus to be $expected, but was $scheduleStatus")
      }
    }
    return this
  }
}
