package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.conversation

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ConversationResponse
import java.time.OffsetDateTime

fun assertThat(actual: ConversationResponse?) = ConversationResponseAssert(actual)

/**
 * AssertJ custom assertion for [ConversationResponse]
 */
class ConversationResponseAssert(actual: ConversationResponse?) :
  AbstractObjectAssert<ConversationResponseAssert, ConversationResponse?>(actual, ConversationResponseAssert::class.java) {

  fun isForPrisonNumber(expected: String): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun hasNoteContent(expected: String): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (note != expected) {
        failWithMessage("Expected note to be $expected, but was $note")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun hasCreatedByDisplayName(expected: String): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAfter(dateTime: OffsetDateTime): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (!createdAt.isAfter(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedAfter(dateTime: OffsetDateTime): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (!updatedAt.isAfter(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun hasUpdatedByDisplayName(expected: String): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): ConversationResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }
}
