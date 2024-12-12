package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.timeline

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.TimelineResponse
import java.util.function.Consumer

fun assertThat(actual: TimelineResponse?) = TimelineResponseAssert(actual)

/**
 * AssertJ custom assertion for [TimelineResponse]
 */
class TimelineResponseAssert(actual: TimelineResponse?) :
  AbstractObjectAssert<TimelineResponseAssert, TimelineResponse?>(actual, TimelineResponseAssert::class.java) {

  fun isForPrisonNumber(expected: String): TimelineResponseAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun hasNumberOfEvents(numberOfEvents: Int): TimelineResponseAssert {
    isNotNull
    with(actual!!) {
      if (events.size != numberOfEvents) {
        failWithMessage("Expected Timeline to be have $numberOfEvents events, but has ${events.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [TimelineEventResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [TimelineEventResponseAssert].
   * Returns this [TimelineResponseAssert] to allow further chained assertions on the parent [TimelineResponse]
   *
   * The `eventNumber` parameter is not zero indexed to make for better readability in tests. IE. the first event
   * should be referenced as `.event(1) { .... }`
   */
  fun event(eventNumber: Int, consumer: Consumer<TimelineEventResponseAssert>): TimelineResponseAssert {
    isNotNull
    with(actual!!) {
      val event = events[eventNumber - 1]
      consumer.accept(assertThat(event))
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [TimelineEventResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [TimelineEventResponseAssert].
   * Returns this [TimelineResponseAssert] to allow further chained assertions on the parent [TimelineResponse]
   *
   * The `eventNumber` parameter is not zero indexed to make for better readability in tests. IE. the first event
   * should be referenced as `.event(1) { .... }`
   */
  fun anyOfEventNumber(vararg eventNumbers: Int, consumer: Consumer<TimelineEventResponseAssert>): TimelineResponseAssert {
    isNotNull
    with(actual!!) {
      val events = events.filterIndexed { index, _ -> eventNumbers.contains(index + 1) }
      val assertionPassed = events.any {
        try {
          consumer.accept(assertThat(it))
          true
        } catch (e: AssertionError) {
          false
        }
      }
      if (!assertionPassed) {
        failWithMessage("Expected timeline event to be not present")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [TimelineEventResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [TimelineEventResponseAssert].
   * Returns this [TimelineResponseAssert] to allow further chained assertions on the parent [TimelineResponse]
   * The assertions on all [TimelineEventResponse]s must pass as true.
   */
  fun allEvents(consumer: Consumer<TimelineEventResponseAssert>): TimelineResponseAssert {
    isNotNull
    with(actual!!) {
      events.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  fun eventsHaveSameCorrelation(): TimelineResponseAssert {
    isNotNull
    with(actual!!) {
      val sharedCorrelationId = events[0].correlationId
      val allMatch = events.all { it.correlationId == sharedCorrelationId }
      if (!allMatch) {
        failWithMessage("Expected Timeline events to have the same correlationId")
      }
    }
    return this
  }
}
