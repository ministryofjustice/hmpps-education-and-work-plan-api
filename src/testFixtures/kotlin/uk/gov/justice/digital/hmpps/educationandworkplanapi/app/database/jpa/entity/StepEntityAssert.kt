package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import org.assertj.core.api.AbstractObjectAssert
import java.time.Instant
import java.util.UUID

fun assertThat(actual: StepEntity?) = StepEntityAssert(actual)

/**
 * AssertJ custom assertion for [StepEntity]
 */
class StepEntityAssert(actual: StepEntity?) :
  AbstractObjectAssert<StepEntityAssert, StepEntity?>(actual, StepEntityAssert::class.java) {

  fun hasJpaManagedFieldsPopulated(): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null || createdBy == null || updatedAt == null || updatedBy == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null || createdBy != null || updatedAt != null || updatedBy != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun hasId(expected: UUID): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: Instant): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }
  fun hasTitle(expected: String): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (title != expected) {
        failWithMessage("Expected title to be $expected, but was $title")
      }
    }
    return this
  }

  fun hasTargetDateRange(expected: TargetDateRange): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (targetDateRange != expected) {
        failWithMessage("Expected targetDateRange to be $expected, but was $targetDateRange")
      }
    }
    return this
  }

  fun hasStatus(expected: StepStatus): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (status != expected) {
        failWithMessage("Expected status to be $expected, but was $status")
      }
    }
    return this
  }

  fun hasReference(expected: UUID): StepEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }
}
