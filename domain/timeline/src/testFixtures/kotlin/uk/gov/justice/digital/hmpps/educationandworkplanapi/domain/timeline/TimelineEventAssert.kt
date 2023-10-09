package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline

import org.assertj.core.api.AbstractObjectAssert
import java.util.UUID

fun assertThat(actual: TimelineEvent?) = TimelineEventAssert(actual)

/**
 * AssertJ custom assertion for [TimelineEvent]
 */
class TimelineEventAssert(actual: TimelineEvent?) :
  AbstractObjectAssert<TimelineEventAssert, TimelineEvent?>(actual, TimelineEventAssert::class.java) {

  fun wasActionedBy(expected: String): TimelineEventAssert {
    isNotNull
    with(actual!!) {
      if (actionedBy != expected) {
        failWithMessage("Expected actionedBy to be $expected, but was $actionedBy")
      }
    }
    return this
  }

  fun wasActionedByDisplayName(expected: String): TimelineEventAssert {
    isNotNull
    with(actual!!) {
      if (actionedByDisplayName != expected) {
        failWithMessage("Expected actionedByDisplayName to be $expected, but was $actionedByDisplayName")
      }
    }
    return this
  }

  fun hasPrisonId(expected: String): TimelineEventAssert {
    isNotNull
    with(actual!!) {
      if (prisonId != expected) {
        failWithMessage("Expected prisonId to be $expected, but was $prisonId")
      }
    }
    return this
  }
  fun hasEventType(expected: TimelineEventType): TimelineEventAssert {
    isNotNull
    with(actual!!) {
      if (eventType != expected) {
        failWithMessage("Expected eventType to be $expected, but was $eventType")
      }
    }
    return this
  }

  fun hasSourceReference(expected: String): TimelineEventAssert {
    isNotNull
    with(actual!!) {
      if (sourceReference != expected) {
        failWithMessage("Expected sourceReference to be $expected, but was $sourceReference")
      }
    }
    return this
  }

  fun hasContextualInfo(expected: String): TimelineEventAssert {
    isNotNull
    with(actual!!) {
      if (contextualInfo != expected) {
        failWithMessage("Expected contextualInfo to be $expected, but was $contextualInfo")
      }
    }
    return this
  }

  fun hasNoContextualInfo(): TimelineEventAssert {
    isNotNull
    with(actual!!) {
      if (contextualInfo != null) {
        failWithMessage("Expected contextualInfo to be null, but was $contextualInfo")
      }
    }
    return this
  }

  fun hasCorrelationId(expected: UUID): TimelineEventAssert {
    isNotNull
    with(actual!!) {
      if (correlationId != expected) {
        failWithMessage("Expected correlationId to be $expected, but was $correlationId")
      }
    }
    return this
  }
}
