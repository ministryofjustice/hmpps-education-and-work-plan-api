package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert
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

  /**
   * Allows for assertion chaining into the specified child [TimelineEventResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [TimelineEventResponseAssert].
   * Returns this [TimelineResponseAssert] to allow further chained assertions on the parent [TimelineResponse]
   */
  fun event(eventNumber: Int, consumer: Consumer<TimelineEventResponseAssert>): TimelineResponseAssert {
    isNotNull
    with(actual!!) {
      val event = events[eventNumber]
      consumer.accept(assertThat(event))
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
}
