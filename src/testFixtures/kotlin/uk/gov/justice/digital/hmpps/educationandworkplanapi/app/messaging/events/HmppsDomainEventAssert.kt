package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.events

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging.EventPublisher.Identifier
import java.time.LocalDate
import java.util.function.Consumer

fun assertThat(actual: HmppsDomainEvent?) = HmppsDomainEventAssert(actual)
fun assertThat(actual: Identifier?) = IdentifierAssert(actual)
fun assertThat(actual: List<HmppsDomainEvent>?) = HmppsDomainEventsAssert(actual)

/**
 * AssertJ custom assertion for a single [HmppsDomainEvent].
 */
class HmppsDomainEventAssert(actual: HmppsDomainEvent?) :
  AbstractObjectAssert<HmppsDomainEventAssert, HmppsDomainEvent?>(
    actual,
    HmppsDomainEventAssert::class.java,
  ) {

  fun hasVersion(expected: Int): HmppsDomainEventAssert {
    isNotNull
    with(actual!!) {
      if (version != expected) {
        failWithMessage("Expected version to be $expected, but was $version")
      }
    }
    return this
  }

  fun hasEventType(expected: String): HmppsDomainEventAssert {
    isNotNull
    with(actual!!) {
      if (eventType != expected) {
        failWithMessage("Expected eventType to be $expected, but was $eventType")
      }
    }
    return this
  }

  fun hasDescription(expected: String): HmppsDomainEventAssert {
    isNotNull
    with(actual!!) {
      if (description != expected) {
        failWithMessage("Expected description to be $expected, but was $description")
      }
    }
    return this
  }

  fun hasDetailUrl(expected: String): HmppsDomainEventAssert {
    isNotNull
    with(actual!!) {
      if (detailUrl != expected) {
        failWithMessage("Expected detailUrl to be $expected, but was $detailUrl")
      }
    }
    return this
  }

  fun occurredAt(expected: LocalDate): HmppsDomainEventAssert {
    isNotNull
    with(actual!!) {
      if (occurredAt != expected) {
        failWithMessage("Expected occurredAt to be $expected, but was $occurredAt")
      }
    }
    return this
  }

  fun hasNumberOfPersonReferenceIdentifiers(expected: Int): HmppsDomainEventAssert {
    isNotNull
    with(actual!!) {
      if (personReference.identifiers.size != expected) {
        failWithMessage("Expected $expected person references, but was ${personReference.identifiers.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [Identifier]. Takes a lambda as the method argument
   * to call assertion methods provided by [IdentifierAssert].
   * Returns this [HmppsDomainEventAssert] to allow further chained assertions on the parent [HmppsDomainEvent].
   *
   * The `person` parameter is not zero indexed to make for better readability in tests. IE. the first event
   * should be referenced as `.person(1) { .... }`
   */
  fun personReferenceIdentifier(person: Int, consumer: Consumer<IdentifierAssert>): HmppsDomainEventAssert {
    isNotNull
    with(actual!!) {
      consumer.accept(assertThat(personReference.identifiers[person - 1]))
    }
    return this
  }
}

/**
 * AssertJ custom assertion for a list of [HmppsDomainEvent]s.
 */
class HmppsDomainEventsAssert(actual: List<HmppsDomainEvent>?) :
  AbstractObjectAssert<HmppsDomainEventsAssert, List<HmppsDomainEvent>?>(
    actual,
    HmppsDomainEventsAssert::class.java,
  ) {

  fun hasNumberOfEvents(expected: Int): HmppsDomainEventsAssert {
    isNotNull
    with(actual!!) {
      if (size != expected) {
        failWithMessage("Expected $expected events, but was $size")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [HmppsDomainEvent]. Takes a lambda as the method argument
   * to call assertion methods provided by [HmppsDomainEventAssert].
   * Returns this [HmppsDomainEventsAssert] to allow further chained assertions on the parent list of [HmppsDomainEvent]s.
   *
   * The `eventNumber` parameter is not zero indexed to make for better readability in tests. IE. the first event
   * should be referenced as `.event(1) { .... }`
   */
  fun event(eventNumber: Int, consumer: Consumer<HmppsDomainEventAssert>): HmppsDomainEventsAssert {
    isNotNull
    with(actual!!) {
      val event = this[eventNumber - 1]
      consumer.accept(assertThat(event))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [HmppsDomainEvent]s. Takes a lambda as the method argument
   * to call assertion methods provided by [HmppsDomainEventAssert].
   * Returns this [HmppsDomainEventsAssert] to allow further chained assertions on the parent list of [HmppsDomainEvent]s
   * The assertions on all [HmppsDomainEvent]s must pass as true.
   */
  fun allEvents(consumer: Consumer<HmppsDomainEventAssert>): HmppsDomainEventsAssert {
    isNotNull
    with(actual!!) {
      this.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }
}

/**
 * AssertJ custom assertion for a single [Identifier].
 */
class IdentifierAssert(actual: Identifier?) :
  AbstractObjectAssert<IdentifierAssert, Identifier?>(
    actual,
    IdentifierAssert::class.java,
  ) {

  fun hasType(expected: String): IdentifierAssert {
    isNotNull
    with(actual!!) {
      if (type != expected) {
        failWithMessage("Expected type to be $expected, but was $type")
      }
    }
    return this
  }

  fun hasValue(expected: String): IdentifierAssert {
    isNotNull
    with(actual!!) {
      if (value != expected) {
        failWithMessage("Expected value to be $expected, but was $value")
      }
    }
    return this
  }
}
