package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import org.assertj.core.api.AbstractObjectAssert
import java.time.Instant
import java.util.UUID

fun assertThat(actual: PreviousQualificationsEntity?) = PreviousQualificationsEntityAssert(actual)

/**
 * AssertJ custom assertion for [PreviousQualificationsEntity]
 */
class PreviousQualificationsEntityAssert(actual: PreviousQualificationsEntity?) :
  AbstractObjectAssert<PreviousQualificationsEntityAssert, PreviousQualificationsEntity?>(
    actual,
    PreviousQualificationsEntityAssert::class.java,
  ) {

  companion object {
    // JPA managed fields, plus the reference field, which are all managed/generated within the API
    private val INTERNALLY_MANAGED_FIELDS =
      arrayOf(
        ".*id",
        ".*reference",
        ".*createdAt",
        ".*createdBy",
        ".*createdByDisplayName",
        ".*updatedAt",
        ".*updatedBy",
        ".*updatedByDisplayName",
      )
  }

  fun hasJpaManagedFieldsPopulated(): PreviousQualificationsEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null || createdBy == null || updatedAt == null || updatedBy == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): PreviousQualificationsEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null || createdBy != null || updatedAt != null || updatedBy != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun hasId(expected: UUID): PreviousQualificationsEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): PreviousQualificationsEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): PreviousQualificationsEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): PreviousQualificationsEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: Instant): PreviousQualificationsEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun hasAReference(): PreviousQualificationsEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference == null) {
        failWithMessage("Expected reference to be populated, but was $reference")
      }
    }
    return this
  }

  fun isEqualToComparingAllFields(expected: PreviousQualificationsEntity): PreviousQualificationsEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .isEqualTo(expected)
    return this
  }

  fun isEqualToIgnoringInternallyManagedFields(expected: PreviousQualificationsEntity): PreviousQualificationsEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(*INTERNALLY_MANAGED_FIELDS)
      .isEqualTo(expected)
    return this
  }
}
