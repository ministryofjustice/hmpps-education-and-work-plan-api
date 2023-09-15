package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import org.assertj.core.api.AbstractObjectAssert
import java.time.Instant
import java.util.UUID

fun assertThat(actual: TimelineEventEntity?) = TimelineEventEntityAssert(actual)

/**
 * AssertJ custom assertion for [TimelineEventEntity]
 */
class TimelineEventEntityAssert(actual: TimelineEventEntity?) :
  AbstractObjectAssert<TimelineEventEntityAssert, TimelineEventEntity?>(actual, TimelineEventEntityAssert::class.java) {

  fun hasJpaManagedFieldsPopulated(): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt")
      }
    }
    return this
  }

  fun hasId(expected: UUID): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun hasAReference(): TimelineEventEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference == null) {
        failWithMessage("Expected reference to be populated, but was $reference")
      }
    }
    return this
  }
}
