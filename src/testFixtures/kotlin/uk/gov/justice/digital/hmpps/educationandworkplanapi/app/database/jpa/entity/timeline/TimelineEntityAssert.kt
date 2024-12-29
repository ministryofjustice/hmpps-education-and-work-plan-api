package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline

import org.assertj.core.api.AbstractObjectAssert
import java.time.Instant
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: TimelineEntity?) = TimelineEntityAssert(actual)

/**
 * AssertJ custom assertion for [TimelineEntity]
 */
class TimelineEntityAssert(actual: TimelineEntity?) :
  AbstractObjectAssert<TimelineEntityAssert, TimelineEntity?>(actual, TimelineEntityAssert::class.java) {

  fun hasJpaManagedFieldsPopulated(): TimelineEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): TimelineEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt")
      }
    }
    return this
  }

  fun hasId(expected: UUID): TimelineEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun hasPrisonNumber(expected: String): TimelineEntityAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): TimelineEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun hasNumberOfEvents(numberOfEvents: Int): TimelineEntityAssert {
    isNotNull
    with(actual!!) {
      if (events.size != numberOfEvents) {
        failWithMessage("Expected Timeline to be have $numberOfEvents events, but has ${events.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [TimelineEventEntity]. Takes a lambda as the method argument
   * to call assertion methods provided by [TimelineEventEntityAssert].
   * Returns this [TimelineEntityAssert] to allow further chained assertions on the parent [TimelineEntity].
   */
  fun event(eventNumber: Int, consumer: Consumer<TimelineEventEntityAssert>): TimelineEntityAssert {
    isNotNull
    with(actual!!) {
      val event = events[eventNumber]
      consumer.accept(assertThat(event))
    }
    return this
  }
}
