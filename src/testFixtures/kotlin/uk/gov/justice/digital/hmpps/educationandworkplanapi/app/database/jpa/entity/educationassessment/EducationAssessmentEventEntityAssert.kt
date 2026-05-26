package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.educationassessment

import org.assertj.core.api.AbstractObjectAssert
import java.time.LocalDate
import java.util.function.Consumer

fun assertThat(actual: EducationAssessmentEventEntity?) = EducationAssessmentEventEntityAssert(actual)
fun assertThat(actual: List<EducationAssessmentEventEntity>?) = EducationAssessmentEventEntitiesAssert(actual)

/**
 * AssertJ custom assertion for a single [EducationAssessmentEventEntity].
 */
class EducationAssessmentEventEntityAssert(actual: EducationAssessmentEventEntity?) :
  AbstractObjectAssert<EducationAssessmentEventEntityAssert, EducationAssessmentEventEntity?>(
    actual,
    EducationAssessmentEventEntityAssert::class.java,
  ) {

  fun hasPrisonNumber(expected: String): EducationAssessmentEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun hasStatus(expected: EducationAssessmentEventStatus): EducationAssessmentEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (status != expected) {
        failWithMessage("Expected status to be $expected, but was $status")
      }
    }
    return this
  }

  fun hasStatusChangeDate(expected: LocalDate): EducationAssessmentEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (statusChangeDate != expected) {
        failWithMessage("Expected statusChangeDate to be $expected, but was $statusChangeDate")
      }
    }
    return this
  }

  fun hasSource(expected: String): EducationAssessmentEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (source != expected) {
        failWithMessage("Expected source to be $expected, but was $source")
      }
    }
    return this
  }

  fun hasDetailUrl(expected: String): EducationAssessmentEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (detailUrl != expected) {
        failWithMessage("Expected detailUrl to be $expected, but was $detailUrl")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): EducationAssessmentEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): EducationAssessmentEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }
}

/**
 * AssertJ custom assertion for a list of [EducationAssessmentEventEntity]s.
 */
class EducationAssessmentEventEntitiesAssert(actual: List<EducationAssessmentEventEntity>?) :
  AbstractObjectAssert<EducationAssessmentEventEntitiesAssert, List<EducationAssessmentEventEntity>?>(
    actual,
    EducationAssessmentEventEntitiesAssert::class.java,
  ) {

  fun hasNumberOfEvents(expected: Int): EducationAssessmentEventEntitiesAssert {
    isNotNull
    with(actual!!) {
      if (size != expected) {
        failWithMessage("Expected $expected events, but was $size")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [EducationAssessmentEventEntity]. Takes a lambda as the method
   * argument to call assertion methods provided by [EducationAssessmentEventEntityAssert].
   * Returns this [EducationAssessmentEventEntitiesAssert] to allow further chained assertions on the parent list.
   *
   * The `eventNumber` parameter is not zero indexed to make for better readability in tests. IE. the first event
   * should be referenced as `.event(1) { .... }`
   */
  fun event(eventNumber: Int, consumer: Consumer<EducationAssessmentEventEntityAssert>): EducationAssessmentEventEntitiesAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(this[eventNumber - 1]))
    }
    return this
  }

  fun hasStatusChangeDatesInAnyOrder(vararg expected: LocalDate): EducationAssessmentEventEntitiesAssert {
    isNotNull
    with(actual!!) {
      val actualDates = map { it.statusChangeDate }
      if (actualDates.sorted() != expected.toList().sorted()) {
        failWithMessage("Expected statusChangeDates to contain ${expected.toList()} in any order, but was $actualDates")
      }
    }
    return this
  }
}
