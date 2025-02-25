package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.InductionScheduleStatus
import java.time.LocalDate
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

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
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

  fun wasUpdatedAtOrAfter(dateTime: OffsetDateTime): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
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

  fun wasInductionPerformedBy(expected: String): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (inductionPerformedBy != expected) {
        failWithMessage("Expected inductionPerformedBy to be $expected, but was $inductionPerformedBy")
      }
    }
    return this
  }
  fun wasInductionPerformedByRole(expected: String): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (inductionPerformedByRole != expected) {
        failWithMessage("Expected inductionPerformedByRole to be $expected, but was $inductionPerformedByRole")
      }
    }
    return this
  }

  fun wasInductionPerformedAt(date: LocalDate): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (inductionPerformedAt != date) {
        failWithMessage("Expected inductionPerformedAt to be equal to $date, but was $inductionPerformedAt")
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

  fun wasVersion(expected: Int): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (version != expected) {
        failWithMessage("Expected version to be $expected, but was $version")
      }
    }
    return this
  }

  fun hasDeadlineDate(expected: LocalDate): InductionScheduleResponseAssert {
    isNotNull
    with(actual!!) {
      if (deadlineDate != expected) {
        failWithMessage("Expected deadline date to be $expected, but was $deadlineDate")
      }
    }
    return this
  }
}
