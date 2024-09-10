package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.INTERNALLY_MANAGED_FIELDS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalEntity
import java.time.Instant
import java.util.UUID

fun assertThat(actual: ConversationNoteEntity?) = ConversationNoteEntityAssert(actual)

class ConversationNoteEntityAssert(actual: ConversationNoteEntity?) : AbstractObjectAssert<ConversationNoteEntityAssert, ConversationNoteEntity?>(actual, ConversationNoteEntityAssert::class.java) {

  fun hasJpaManagedIdFieldPopulated(): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null) {
        failWithMessage("Expected entity to have the JPA managed ID field populated, but was it was null")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedIdFieldPopulated(): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null) {
        failWithMessage("Expected entity not to have the JPA managed ID field populated, but was [id = $id]")
      }
    }
    return this
  }

  fun isEqualToComparingAllFields(expected: GoalEntity): ConversationNoteEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .isEqualTo(expected)
    return this
  }

  fun isEqualToIgnoringJpaManagedFields(expected: GoalEntity): ConversationNoteEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(*INTERNALLY_MANAGED_FIELDS)
      .isEqualTo(expected)
    return this
  }

  fun hasId(expected: UUID): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun hasCreatedByDisplayName(expected: String): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: Instant): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt!!.isBefore(dateTime)) {
        failWithMessage("Expected createdAt to be at or after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun hasUpdatedByDisplayName(expected: String): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: Instant): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: Instant): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt!!.isBefore(dateTime)) {
        failWithMessage("Expected updatedAt to be at or after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  fun hasContent(expected: String): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (content != expected) {
        failWithMessage("Expected content to be $expected, but was $content")
      }
    }
    return this
  }

  fun hasReference(expected: UUID): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun hasAReference(): ConversationNoteEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference == null) {
        failWithMessage("Expected reference to be populated, but was $reference")
      }
    }
    return this
  }
}
