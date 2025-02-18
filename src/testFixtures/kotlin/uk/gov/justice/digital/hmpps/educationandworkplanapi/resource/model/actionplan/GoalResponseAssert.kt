package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import org.assertj.core.api.AbstractObjectAssert
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.NoteType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.StepStatus
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*
import java.util.function.Consumer

fun assertThat(actual: GoalResponse?) = GoalResponseAssert(actual)

/**
 * AssertJ custom assertion for a single [GoalResponse]
 */
class GoalResponseAssert(actual: GoalResponse?) : AbstractObjectAssert<GoalResponseAssert, GoalResponse?>(actual, GoalResponseAssert::class.java) {

  fun wasCreatedBy(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdBy != expected) {
        failWithMessage("Expected createdBy to be $expected, but was $createdBy")
      }
    }
    return this
  }

  fun hasCreatedByDisplayName(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdByDisplayName != expected) {
        failWithMessage("Expected createdByDisplayName to be $expected, but was $createdByDisplayName")
      }
    }
    return this
  }

  fun wasCreatedAt(expected: OffsetDateTime): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAt != expected) {
        failWithMessage("Expected createdAt to be $expected, but was $createdAt")
      }
    }
    return this
  }

  fun wasCreatedAtPrison(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (createdAtPrison != expected) {
        failWithMessage("Expected createdAtPrison to be $expected, but was $createdAtPrison")
      }
    }
    return this
  }

  fun wasUpdatedBy(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedBy != expected) {
        failWithMessage("Expected updatedBy to be $expected, but was $updatedBy")
      }
    }
    return this
  }

  fun hasUpdatedByDisplayName(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedByDisplayName != expected) {
        failWithMessage("Expected updatedByDisplayName to be $expected, but was $updatedByDisplayName")
      }
    }
    return this
  }

  fun wasUpdatedAt(expected: OffsetDateTime): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAt != expected) {
        failWithMessage("Expected updatedAt to be $expected, but was $updatedAt")
      }
    }
    return this
  }

  fun wasUpdatedAtPrison(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (updatedAtPrison != expected) {
        failWithMessage("Expected updatedAtPrison to be $expected, but was $updatedAtPrison")
      }
    }
    return this
  }

  fun hasTitle(expected: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (title != expected) {
        failWithMessage("Expected title to be $expected, but was $title")
      }
    }
    return this
  }

  fun hasTargetCompletionDate(expected: LocalDate): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (targetCompletionDate != expected) {
        failWithMessage("Expected targetCompletionDate to be $expected, but was $targetCompletionDate")
      }
    }
    return this
  }

  fun hasNumberOfSteps(expected: Int): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (steps.size != expected) {
        failWithMessage("Expected goal to have $expected Steps, but was ${steps.size}")
      }
    }
    return this
  }

  fun hasNoNotes(): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (goalNotes.isNotEmpty()) {
        failWithMessage("Expected goal to have no notes, but there were ${goalNotes.size}")
      }
    }
    return this
  }

  fun hasNoGoalNote(): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      val goalNotesCount = goalNotes.count { it.type == NoteType.GOAL }
      if (goalNotesCount > 0) {
        failWithMessage("Expected goal to have no goal notes, but there were $goalNotesCount")
      }
    }
    return this
  }

  fun hasNoArchiveNote(): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      val archiveNotesCount = goalNotes.count { it.type == NoteType.GOAL_ARCHIVAL }
      if (archiveNotesCount > 0) {
        failWithMessage("Expected goal to have no archive notes, but there were $archiveNotesCount")
      }
    }
    return this
  }

  fun hasNoCompletedNote(): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      val completionNotesCount = goalNotes.count { it.type == NoteType.GOAL_COMPLETION }
      if (completionNotesCount > 0) {
        failWithMessage("Expected goal to have no completion notes, but there were $completionNotesCount")
      }
    }
    return this
  }

  fun hasCompletedSteps(): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      val incompleteStepsCount = steps.count { it.status != StepStatus.COMPLETE }
      if (incompleteStepsCount > 0) {
        failWithMessage("Expected goal to have no incomplete steps, but there were $incompleteStepsCount")
      }
    }
    return this
  }

  fun hasReference(expected: UUID): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (goalReference != expected) {
        failWithMessage("Expected reference to be $expected, but was $goalReference")
      }
    }
    return this
  }

  fun hasCompletedNote(expected: String): GoalResponseAssert = hasNoteOfType(NoteType.GOAL_COMPLETION, expected, "completion")

  fun hasArchiveNote(expected: String): GoalResponseAssert = hasNoteOfType(NoteType.GOAL_ARCHIVAL, expected, "archive")

  fun hasGoalNote(expected: String): GoalResponseAssert = hasNoteOfType(NoteType.GOAL, expected, "goal")

  private fun hasNoteOfType(noteType: NoteType, expected: String, noteLabel: String): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      val note = goalNotes.firstOrNull { it.type == noteType }
      when {
        note == null ->
          failWithMessage("Expected $noteLabel note to be $expected, but was null")

        note.content != expected ->
          failWithMessage("Expected $noteLabel note to be $expected, but was ${note.content}")
      }
    }
    return this
  }

  /**
   * Allows for assertion chaining into the specified child [StepResponse]. Takes a lambda as the method argument
   * to call assertion methods provided by [StepResponseAssert].
   * Returns this [GoalResponseAssert] to allow further chained assertions on the parent [GoalResponse]
   *
   * The `stepNumber` parameter is not zero indexed to make for better readability in tests. IE. the first step
   * should be referenced as `.step(1) { .... }`
   */
  fun step(stepNumber: Int, consumer: Consumer<StepResponseAssert>): GoalResponseAssert {
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
   * Returns this [GoalResponseAssert] to allow further chained assertions on the parent [GoalResponse]
   * The assertions on all [StepResponse]s must pass as true.
   */
  fun allSteps(consumer: Consumer<StepResponseAssert>): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      steps.onEach {
        consumer.accept(assertThat(it))
      }
    }
    return this
  }

  fun hasStatus(expected: GoalStatus): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (status != expected) {
        failWithMessage("Expected goal to have status '$expected', but was $status")
      }
    }
    return this
  }

  fun hasArchiveReason(expected: ReasonToArchiveGoal?): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (archiveReason != expected) {
        failWithMessage("Expected goal to have archive reason '$expected', but was $archiveReason")
      }
    }
    return this
  }

  fun hasArchiveReasonOther(expected: String?): GoalResponseAssert {
    isNotNull
    with(actual!!) {
      if (archiveReasonOther != expected) {
        failWithMessage("Expected goal to have archive reason other '$expected', but was $archiveReasonOther")
      }
    }
    return this
  }
}
