package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.sar

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SarGoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.StepResponseAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.assertThat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.function.Consumer

fun assertThat(actual: SarGoalResponse?) = SarGoalResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [SarGoalResponse]
 */
class SarGoalResponseAssert(actual: SarGoalResponse?) : AbstractObjectAssert<SarGoalResponseAssert, SarGoalResponse?>(actual, SarGoalResponseAssert::class.java) {

  fun wasCreatedBy(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun hasCreatedByDisplayName(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun hasUpdatedByDisplayName(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  fun hasTitle(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (title != expected) {
        failWithMessage("Expected title to be $expected, but was $title")
      }
    }
    return this
  }

  fun hasTargetCompletionDate(expected: LocalDate): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (targetCompletionDate != expected) {
        failWithMessage("Expected targetCompletionDate to be $expected, but was $targetCompletionDate")
      }
    }
    return this
  }

  fun hasNumberOfSteps(expected: Int): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (steps.size != expected) {
        failWithMessage("Expected goal to have $expected Steps, but was ${steps.size}")
      }
    }
    return this
  }

  fun hasNoGoalNote(): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (!goalNote.isNullOrBlank()) {
        failWithMessage("Expected goal to have no goal notes, but had note $goalNote")
      }
    }
    return this
  }

  fun hasNoArchiveNote(): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (!goalArchiveNote.isNullOrBlank()) {
        failWithMessage("Expected goal to have no archive notes, but had note $goalArchiveNote")
      }
    }
    return this
  }

  fun hasNoCompletedNote(): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (!goalCompletionNote.isNullOrBlank()) {
        failWithMessage("Expected goal to have no completion notes, but had note $goalCompletionNote")
      }
    }
    return this
  }

  fun hasCompletedSteps(): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      val incompleteStepsCount = steps.count { it.status != StepStatus.COMPLETE }
      if (incompleteStepsCount > 0) {
        failWithMessage("Expected goal to have no incomplete steps, but there were $incompleteStepsCount")
      }
    }
    return this
  }

  fun hasGoalNote(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (goalNote != expected) {
        failWithMessage("Expected goal to have goal note $expected, but was $goalNote")
      }
    }
    return this
  }

  fun hasArchiveNote(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (goalArchiveNote != expected) {
        failWithMessage("Expected goal to have archive note $expected, but was $goalArchiveNote")
      }
    }
    return this
  }

  fun hasCompletedNote(expected: String): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (goalCompletionNote != expected) {
        failWithMessage("Expected goal to have completion note $expected, but was $goalCompletionNote")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [StepResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [StepResponseAssert].
   * Returns this [SarGoalResponseAssert] to allow further chained assertions on the parent [SarGoalResponse]
   *
   * The `stepNumber` parameter is not zero indexed to make for better readability in tests. IE. the first step
   * should be referenced as `.step(1) { .... }`
   */
  fun step(stepNumber: Int, consumer: Consumer<StepResponseAssert>): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      val step = steps[stepNumber - 1]
      consumer.accept(assertThat(step))
    }
    return this
  }

  /**
   * Allows for assertion chaining into all child [StepResponse]s. Takes a lambda as the method argument
   * to call assertion methods provided by [SteoResponseAssert].
   * Returns this [SarGoalResponseAssert] to allow further chained assertions on the parent [SarGoalResponse]
   * The assertions on all [StepResponse]s must pass as true.
   */
  fun allSteps(consumer: Consumer<StepResponseAssert>): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      steps.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  fun hasStatus(expected: GoalStatus): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (status != expected) {
        failWithMessage("Expected goal to have status '$expected', but was $status")
      }
    }
    return this
  }

  fun hasArchiveReason(expected: ReasonToArchiveGoal?): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (goalArchiveReason != expected) {
        failWithMessage("Expected goal to have archive reason '$expected', but was $goalArchiveReason")
      }
    }
    return this
  }

  fun hasArchiveReasonOther(expected: String?): SarGoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (goalArchiveReasonOther != expected) {
        failWithMessage("Expected goal to have archive reason other '$expected', but was $goalArchiveReasonOther")
      }
    }
    return this
  }
}
