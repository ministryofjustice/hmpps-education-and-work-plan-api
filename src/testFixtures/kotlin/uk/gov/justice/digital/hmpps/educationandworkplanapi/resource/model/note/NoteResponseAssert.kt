package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.note

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType
import java.time.OffsetDateTime
import java.util.UUID

fun assertThat(actual: NoteResponse?) = NoteResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [NoteResponse].
 */
class NoteResponseAssert(actual: NoteResponse?) :
  AbstractObjectAssert<NoteResponseAssert, NoteResponse?>(
    actual,
    NoteResponseAssert::class.java,
  ) {

  fun hasReference(expected: UUID): NoteResponseAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): NoteResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtOrAfter(dateTime: OffsetDateTime): NoteResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt.isBefore(dateTime)) {
        failWithMessage("Expected createdAt to be after $dateTime, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): NoteResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedByDisplayName(expected: String): NoteResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): NoteResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun hasContent(expected: String): NoteResponseAssert {
    isNotNull
    with(actual!!) {
      if (content != expected) {
        failWithMessage("Expected content to be $expected, but was $content")
      }
    }
    return this
  }

  fun hasType(expected: NoteType): NoteResponseAssert {
    isNotNull
    with(actual!!) {
      if (type != expected) {
        failWithMessage("Expected type to be $expected, but was $type")
      }
    }
    return this
  }
}
