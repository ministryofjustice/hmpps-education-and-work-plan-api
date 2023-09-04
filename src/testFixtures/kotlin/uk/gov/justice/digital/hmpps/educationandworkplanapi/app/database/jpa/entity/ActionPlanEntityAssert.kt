package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import org.assertj.core.api.AbstractObjectAssert
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.function.Consumer

fun assertThat(actual: ActionPlanEntity?) = ActionPlanEntityAssert(actual)

/**
 * AssertJ custom assertion for [ActionPlanEntity]
 */
class ActionPlanEntityAssert(actual: ActionPlanEntity?) :
  AbstractObjectAssert<ActionPlanEntityAssert, ActionPlanEntity?>(actual, ActionPlanEntityAssert::class.java) {

  fun hasJpaManagedFieldsPopulated(): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (id == null || createdAt == null || createdBy == null || updatedAt == null || updatedBy == null) {
        failWithMessage("Expected entity to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun doesNotHaveJpaManagedFieldsPopulated(): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != null || createdAt != null || createdBy != null || updatedAt != null || updatedBy != null) {
        failWithMessage("Expected entity not to have the JPA managed fields populated, but was [id = $id, createdAt = $createdAt, createdBy = $createdBy, updatedAt = $updatedAt, updatedBy = $updatedBy")
      }
    }
    return this
  }

  fun hasId(expected: UUID): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (id != expected) {
        failWithMessage("Expected id to be $expected, but was $id")
      }
    }
    return this
  }

  fun wasCreatedBy(expected: String): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: Instant): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: Instant): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun isForPrisonNumber(expected: String): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (prisonNumber != expected) {
        failWithMessage("Expected prisonNumber to be $expected, but was $prisonNumber")
      }
    }
    return this
  }

  fun hasReviewDate(expected: LocalDate): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (reviewDate != expected) {
        failWithMessage("Expected reviewDate to be $expected, but was $reviewDate")
      }
    }
    return this
  }

  fun hasNoReviewDate(): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (reviewDate != null) {
        failWithMessage("Expected reviewDate to be null, but was $reviewDate")
      }
    }
    return this
  }

  fun hasNoGoalsSet(): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (goals!!.isNotEmpty()) {
        failWithMessage("Expected ActionPlan to be have no goals set, but has $goals")
      }
    }
    return this
  }

  fun hasNumberOfGoals(numberOfGoals: Int): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (goals!!.size != numberOfGoals) {
        failWithMessage("Expected ActionPlan to be have $numberOfGoals goals set, but has ${goals!!.size}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [GoalEntity]. Takes a lambda as the method argument
   * to call assertion methods provided by [GoalEntityAssert].
   * Returns this [ActionPlanEntityAssert] to allow further chained assertions on the parent [ActionPlanEntity]
   */
  fun goal(goalNumber: Int, consumer: Consumer<GoalEntityAssert>): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      val goal = goals!![goalNumber]
      consumer.accept(assertThat(goal))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [GoalEntity]s. Takes a lambda as the method argument
   * to call assertion methods provided by [GoalEntityAssert].
   * Returns this [ActionPlanEntityAssert] to allow further chained assertions on the parent [ActionPlanEntity]
   * The assertions on all [GoalEntity]s must pass as true.
   */
  fun allGoals(consumer: Consumer<GoalEntityAssert>): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      goals!!.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  fun hasAReference(): ActionPlanEntityAssert {
    isNotNull
    with(actual!!) {
      if (reference == null) {
        failWithMessage("Expected reference to be populated, but was $reference")
      }
    }
    return this
  }
}
