package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.INTERNALLY_MANAGED_FIELDS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import java.time.Instant
import java.util.UUID

fun assertThat(actual: PreviousWorkExperiencesEntity?) = PreviousWorkExperiencesEntityAssert(actual)

/**
 * AssertJ custom assertion for [PreviousWorkExperiencesEntity]
 */
class PreviousWorkExperiencesEntityAssert(actual: PreviousWorkExperiencesEntity?) :
  AbstractObjectAssert<PreviousWorkExperiencesEntityAssert, PreviousWorkExperiencesEntity?>(
    actual,
    PreviousWorkExperiencesEntityAssert::class.java,
  ) {

  fun hasJpaManagedFieldsPopulated(): PreviousWorkExperiencesEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null || createdBy == null || updatedAt == null || updatedBy == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): PreviousWorkExperiencesEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null || createdBy != null || updatedAt != null || updatedBy != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun hasId(expected: UUID): PreviousWorkExperiencesEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): PreviousWorkExperiencesEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): PreviousWorkExperiencesEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): PreviousWorkExperiencesEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: Instant): PreviousWorkExperiencesEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: Instant): PreviousWorkExperiencesEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt!!.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun isEqualToComparingAllFields(expected: PreviousWorkExperiencesEntity): PreviousWorkExperiencesEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .isEqualTo(expected)
    return this
  }

  fun isEqualToIgnoringInternallyManagedFields(expected: PreviousWorkExperiencesEntity): PreviousWorkExperiencesEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(*INTERNALLY_MANAGED_FIELDS)
      .isEqualTo(expected)
    return this
  }
}
