package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.timeline

import org.assertj.core.api.AbstractObjectAssert
import java.time.Instant
import java.util.UUID

fun assertThat(actual: TimelineEventEntity?) = TimelineEventEntityAssert(actual)

/**
 * AssertJ custom assertion for [TimelineEventEntity]
 */
class TimelineEventEntityAssert(actual: TimelineEventEntity?) : AbstractObjectAssert<TimelineEventEntityAssert, TimelineEventEntity?>(actual, TimelineEventEntityAssert::class.java) {

  fun hasJpaManagedFieldsPopulated(): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt")
      }
    }
    return this
  }

  fun hasId(expected: UUID): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasActionedBy(expected: String): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (actionedBy != expected) {
        failWithMessage("Expected actionedBy to be $expected, but was $actionedBy")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun hasPrisonId(expected: String): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (prisonId != expected) {
        failWithMessage("Expected prisonId to be $expected, but was $prisonId")
      }
    }
    return this
  }

  fun hasEventType(expected: TimelineEventType): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (eventType != expected) {
        failWithMessage("Expected eventType to be $expected, but was $eventType")
      }
    }
    return this
  }

  fun hasSourceReference(expected: String): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (sourceReference != expected) {
        failWithMessage("Expected sourceReference to be $expected, but was $sourceReference")
      }
    }
    return this
  }

  fun hasContextualInfo(expected: Map<String, String>): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (contextualInfo != expected) {
        failWithMessage("Expected contextualInfo to be $expected, but was $contextualInfo")
      }
    }
    return this
  }

  fun hasCorrelationId(expected: UUID): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (correlationId != expected) {
        failWithMessage("Expected correlationId to be $expected, but was $correlationId")
      }
    }
    return this
  }
}
