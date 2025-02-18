package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineEventType
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
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

  fun hasNoActionedByDisplayName(): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (actionedByDisplayName != null) {
        failWithMessage("Expected actionedByDisplayName to be null, but was $actionedByDisplayName")
      }
    }
    return this
  }

  // sometimes dates are returned with nanoseconds and sometimes not so we have to do some truncation here in order to
  // get the dates to match.
  fun hasContextualInfo(expected: Map<String, String>): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      val truncatedContextualInfo = contextualInfo.mapValues { truncateToMilliseconds(it.value) }
      val truncatedExpected = expected.mapValues { truncateToMilliseconds(it.value) }
      if (truncatedContextualInfo != truncatedExpected) {
        failWithMessage("Expected contextualInfo to be $truncatedExpected, but was $truncatedContextualInfo")
      }
    }
    return this
  }

  private fun truncateToMilliseconds(value: String): String = try {
    val dateTime = OffsetDateTime.parse(value)
    val nanos = dateTime.nano
    val roundedNanos = if (nanos % 1_000_000 >= 500_000) {
      // Add 1 millisecond if the remaining nanoseconds are >= 500,000
      dateTime.plusNanos(1_000_000L - nanos % 1_000_000)
    } else {
      // Otherwise, subtract the remaining nanoseconds
      dateTime.minusNanos((nanos % 1_000_000).toLong())
    }
    roundedNanos.truncatedTo(ChronoUnit.MILLIS).toString()
  } catch (e: DateTimeParseException) {
    // If parsing fails, return the original string
    value
  }

  fun hasNoContextualInfo(): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (contextualInfo.isNotEmpty()) {
        failWithMessage("Expected contextualInfo to be an empty map, but was $contextualInfo")
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

  fun correlationIdIsNotEqualTo(unexpected: UUID): TimelineEventResponseAssert {
    isNotNull
    with(actual!!) {
      if (correlationId == unexpected) {
        failWithMessage("Expected correlationId NOT to be $unexpected")
      }
    }
    return this
  }
}
