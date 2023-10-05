package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert
import java.util.UUID

fun assertThat(actual: TimelineEventResponse?) = TimelineEventResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [TimelineEventResponse]
 */
class TimelineEventResponseAssert(actual: TimelineEventResponse?) :
  AbstractObjectAssert<TimelineEventResponseAssert, TimelineEventResponse?>(
    actual,
    TimelineEventResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun hasSourceReference(expected: String): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (sourceReference != expected) {
        failWithMessage("Expected sourceReference to be $expected, but was $sourceReference")
      }
    }
    return this
  }

  fun hasEventType(expected: TimelineEventType): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (eventType != expected) {
        failWithMessage("Expected eventType to be $expected, but was $eventType")
      }
    }
    return this
  }

  fun hasPrisonId(expected: String): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (prisonId != expected) {
        failWithMessage("Expected prisonId to be $expected, but was $prisonId")
      }
    }
    return this
  }

  fun wasActionedBy(expected: String): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (actionedBy != expected) {
        failWithMessage("Expected actionedBy to be $expected, but was $actionedBy")
      }
    }
    return this
  }

  fun hasActionedByDisplayName(expected: String): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (actionedByDisplayName != expected) {
        failWithMessage("Expected actionedByDisplayName to be $expected, but was $actionedByDisplayName")
      }
    }
    return this
  }

  fun hasContextualInfo(expected: String): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (contextualInfo != expected) {
        failWithMessage("Expected contextualInfo to be $expected, but was $contextualInfo")
      }
    }
    return this
  }

  fun hasNoContextualInfo(): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (contextualInfo != null) {
        failWithMessage("Expected contextualInfo to be null, but was $contextualInfo")
      }
    }
    return this
  }

  fun hasCorrelationId(expected: UUID): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (correlationId != expected) {
        failWithMessage("Expected correlationId to be $expected, but was $correlationId")
      }
    }
    return this
  }

  fun doesNotHaveCorrelationId(unexpected: UUID): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (correlationId == unexpected) {
        failWithMessage("Expected correlationId NOT to be $unexpected")
      }
    }
    return this
  }
}
