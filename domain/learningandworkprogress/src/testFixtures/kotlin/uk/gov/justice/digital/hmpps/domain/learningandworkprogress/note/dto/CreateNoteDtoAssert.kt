package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.dto

import org.assertj.core.api.AbstractObjectAssert
import java.util.UUID

fun assertThat(actual: CreateNoteDto?) = CreateNoteDtoAssert(actual)

/**
 * AssertJ custom assertion for [CreateNoteDto].
 */
class CreateNoteDtoAssert(actual: CreateNoteDto?) :
  AbstractObjectAssert<CreateNoteDtoAssert, CreateNoteDto?>(actual, CreateNoteDtoAssert::class.java) {

  fun hasPrisonNumber(expected: String): CreateNoteDtoAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun hasContent(expected: String): CreateNoteDtoAssert {
    isNotNull
    with(actual!!) {
      if (content != expected) {
        failWithMessage("Expected note content to be $expected, but was $content")
      }
    }
    return this
  }

  fun hasEntityReference(expected: UUID): CreateNoteDtoAssert {
    isNotNull
    with(actual!!) {
      if (entityReference != expected) {
        failWithMessage("Expected note entityReference to be $expected, but was $entityReference")
      }
    }
    return this
  }

  fun hasEntityType(expected: EntityType): CreateNoteDtoAssert {
    isNotNull
    with(actual!!) {
      if (entityType != expected) {
        failWithMessage("Expected note entityType to be $expected, but was $entityType")
      }
    }
    return this
  }
}
