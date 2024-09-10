package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.INTERNALLY_MANAGED_FIELDS
import java.time.Instant
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: ConversationEntity?) = ConversationEntityAssert(actual)

class ConversationEntityAssert(actual: ConversationEntity?) : AbstractObjectAssert<ConversationEntityAssert, ConversationEntity?>(actual, ConversationEntityAssert::class.java) {

  fun hasJpaManagedIdFieldPopulated(): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null) {
        failWithMessage("Expected entity to have the JPA managed ID field populated, but was it was null")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedIdFieldPopulated(): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null) {
        failWithMessage("Expected entity not to have the JPA managed ID field populated, but was [id = $id]")
      }
    }
    return this
  }

  fun isEqualToComparingAllFields(expected: ConversationEntity): ConversationEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .isEqualTo(expected)
    return this
  }

  fun isEqualToIgnoringJpaManagedFields(expected: ConversationEntity): ConversationEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(*INTERNALLY_MANAGED_FIELDS)
      .isEqualTo(expected)
    return this
  }

  fun hasId(expected: UUID): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun isOfType(expected: ConversationType): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (type != expected) {
        failWithMessage("Expected type to be $expected, but was $actual")
      }
    }
    return this
  }

  fun isForPrisonNumber(expected: String): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: Instant): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt!!.isBefore(dateTime)) {
        failWithMessage("Expected createdAt to be at or after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: Instant): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAfter(dateTime: Instant): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (!updatedAt!!.isAfter(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: Instant): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt!!.isBefore(dateTime)) {
        failWithMessage("Expected updatedAt to be at or after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the child [ConversationNoteEntity]. Takes a lambda as the method argument
   * to call assertion methods provided by [ConversationNoteEntityAssert].
   * Returns this [ConversationEntityAssert] to allow further chained assertions on the parent [ConversationEntity]
   * The assertions on all [ConversationNoteEntity]s must pass as true.
   */
  fun content(consumer: Consumer<ConversationNoteEntityAssert>): ConversationEntityAssert {
    isNotNull
    with(actual!!.note) {
      consumer.accept(
        assertThat(this),
      )
    }
    return this
  }

  fun hasReference(expected: UUID): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun hasAReference(): ConversationEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference == null) {
        failWithMessage("Expected reference to be populated, but was $reference")
      }
    }
    return this
  }
}
