package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.INTERNALLY_MANAGED_FIELDS
import uk.gov.justice.digital.hmpps.educationandworkplanapi.isBeforeRounded
import java.time.Instant
import java.util.UUID

fun assertThat(actual: PersonalSkillsAndInterestsEntity?) = PersonalSkillsAndInterestsEntityAssert(actual)

/**
 * AssertJ custom assertion for [PersonalSkillsAndInterestsEntity]
 */
class PersonalSkillsAndInterestsEntityAssert(actual: PersonalSkillsAndInterestsEntity?) :
  AbstractObjectAssert<PersonalSkillsAndInterestsEntityAssert, PersonalSkillsAndInterestsEntity?>(
    actual,
    PersonalSkillsAndInterestsEntityAssert::class.java,
  ) {

  fun hasJpaManagedFieldsPopulated(): PersonalSkillsAndInterestsEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null || createdBy == null || updatedAt == null || updatedBy == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): PersonalSkillsAndInterestsEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null || createdBy != null || updatedAt != null || updatedBy != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun hasId(expected: UUID): PersonalSkillsAndInterestsEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): PersonalSkillsAndInterestsEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): PersonalSkillsAndInterestsEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): PersonalSkillsAndInterestsEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: Instant): PersonalSkillsAndInterestsEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtOrAfter(dateTime: Instant): PersonalSkillsAndInterestsEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt!!.isBeforeRounded(dateTime)) {
        failWithMessage("Expected updatedAt to be after $dateTime, but was $updatedAt")
      }
    }
    return this
  }

  fun isEqualToComparingAllFields(expected: PersonalSkillsAndInterestsEntity): PersonalSkillsAndInterestsEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .isEqualTo(expected)
    return this
  }

  fun isEqualToIgnoringInternallyManagedFields(expected: PersonalSkillsAndInterestsEntity): PersonalSkillsAndInterestsEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(*INTERNALLY_MANAGED_FIELDS)
      .isEqualTo(expected)
    return this
  }
}
