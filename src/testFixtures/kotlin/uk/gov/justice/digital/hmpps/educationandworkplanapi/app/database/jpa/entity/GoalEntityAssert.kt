package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import org.assertj.core.api.AbstractObjectAssert
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: GoalEntity?) = GoalEntityAssert(actual)

/**
 * AssertJ custom assertion for [GoalEntity]
 */
class GoalEntityAssert(actual: GoalEntity?) :
  AbstractObjectAssert<GoalEntityAssert, GoalEntity?>(actual, GoalEntityAssert::class.java) {

  companion object {
    private val JPA_MANAGED_FIELDS = arrayOf("id", "createdAt", "createdBy", "createdByDisplayName", "updatedAt", "updatedBy", "updatedByDisplayName")
  }

  fun hasJpaManagedFieldsPopulated(): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null || createdBy == null || createdByDisplayName == null || updatedAt == null || updatedBy == null || updatedByDisplayName == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, createdByDisplayName = $createdByDisplayName, updatedAt = $updatedAt, updatedBy = $updatedBy, updatedByDisplayName = $updatedByDisplayName")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null || createdBy != null || createdByDisplayName != null || updatedAt != null || updatedBy != null || updatedByDisplayName != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, createdByDisplayName = $createdByDisplayName, updatedAt = $updatedAt, updatedBy = $updatedBy, updatedByDisplayName = $updatedByDisplayName")
      }
    }
    return this
  }

  fun isEqualToComparingAllFields(expected: GoalEntity): GoalEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .isEqualTo(expected)
    return this
  }

  fun isEqualToIgnoringJpaManagedFields(expected: GoalEntity): GoalEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields(*JPA_MANAGED_FIELDS)
      .isEqualTo(expected)
    return this
  }

  fun isEqualToIgnoringSteps(expected: GoalEntity): GoalEntityAssert {
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("steps")
      .isEqualTo(expected)
    return this
  }

  fun hasId(expected: UUID): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun hasCreatedByDisplayName(expected: String): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun hasUpdatedByDisplayName(expected: String): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: Instant): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  fun hasTitle(expected: String): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (title != expected) {
        failWithMessage("Expected title to be $expected, but was $title")
      }
    }
    return this
  }

  fun hasReviewDate(expected: LocalDate): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (reviewDate != expected) {
        failWithMessage("Expected reviewDate to be $expected, but was $reviewDate")
      }
    }
    return this
  }

  fun hasNumberOfSteps(expected: Int): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (steps!!.size != expected) {
        failWithMessage("Expected goal to have $expected Steps, but was ${steps!!.size}")
      }
    }
    return this
  }

  fun hasReference(expected: UUID): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference != expected) {
        failWithMessage("Expected reference to be $expected, but was $reference")
      }
    }
    return this
  }

  fun hasAReference(): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference == null) {
        failWithMessage("Expected reference to be populated, but was $reference")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [StepEntity]. Takes a lambda as the method argument
   * to call assertion methods provided by [StepEntityAssert].
   * Returns this [GoalEntityAssert] to allow further chained assertions on the parent [GoalEntity]
   */
  fun stepWithSequenceNumber(sequenceNumber: Int, consumer: Consumer<StepEntityAssert>): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      val step = steps!!.find { it.sequenceNumber == sequenceNumber }
      consumer.accept(assertThat(step))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [StepEntity]s. Takes a lambda as the method argument
   * to call assertion methods provided by [StepEntityAssert].
   * Returns this [GoalEntityAssert] to allow further chained assertions on the parent [GoalEntity]
   * The assertions on all [StepEntity]s must pass as true.
   */
  fun allSteps(consumer: Consumer<StepEntityAssert>): GoalEntityAssert {
    isNotNull
    with(actual!!) {
      steps!!.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }
}
