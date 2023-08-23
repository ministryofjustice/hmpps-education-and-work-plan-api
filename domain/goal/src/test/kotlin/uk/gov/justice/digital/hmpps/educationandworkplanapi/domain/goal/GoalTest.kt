package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class GoalTest {

  @Test
  fun `should create Goal given Steps out of sequence`() {
    // Given
    val step1 = aValidStep(sequenceNumber = 1, title = "Book course")
    val step2 = aValidStep(sequenceNumber = 2, title = "Attend course")
    val step3 = aValidStep(sequenceNumber = 3, title = "Pass exam")

    // When
    val goal = aValidGoal(steps = mutableListOf(step2, step3, step1)) // Steps passed into the goal out of sequence

    // Then
    assertThat(goal.steps.map { it.title }).containsExactly( // steps are returned in correct sequence based on sequenceNumber
      "Book course",
      "Attend course",
      "Pass exam",
    )
  }

  @Nested
  inner class AddStep {
    @Test
    fun `should add Step and enforce Step sequence and order`() {
      // Given
      val step1 = aValidStep(sequenceNumber = 1, title = "Book course")
      val step2 = aValidStep(sequenceNumber = 2, title = "Pass exam")
      val goal = aValidGoal(steps = mutableListOf(step1, step2)) // goal contains steps "Book course" and "Pass exam", but not "Attend Course"

      val newStep = aValidStep(sequenceNumber = 2, title = "Attend course")

      // When
      goal.addStep(newStep) // add "Attend course" step

      // Then
      assertThat(goal.steps.map { it.title }).containsExactly( // steps are returned in correct sequence
        "Book course",
        "Attend course",
        "Pass exam",
      )
    }

    @Test
    fun `should add Step and enforce Step sequence and order given new step has a negative sequence number`() {
      // Given
      val step1 = aValidStep(sequenceNumber = 1, title = "Book course")
      val step2 = aValidStep(sequenceNumber = 2, title = "Pass exam")
      val goal = aValidGoal(steps = mutableListOf(step1, step2)) // goal contains steps "Book course" and "Pass exam"

      val newStep = aValidStep(sequenceNumber = -1, title = "The new step")

      // When
      goal.addStep(newStep)

      // Then
      assertThat(goal.steps.map { it.title }).containsExactly( // steps are returned in correct sequence
        "The new step",
        "Book course",
        "Pass exam",
      )
    }

    @Test
    fun `should add Step and enforce Step sequence and order given new step has a sequence number greater than the list size`() {
      // Given
      val step1 = aValidStep(sequenceNumber = 1, title = "Book course")
      val step2 = aValidStep(sequenceNumber = 2, title = "Pass exam")
      val goal = aValidGoal(steps = mutableListOf(step1, step2)) // goal contains steps "Book course" and "Pass exam"

      val newStep = aValidStep(sequenceNumber = 10, title = "The new step")

      // When
      goal.addStep(newStep)

      // Then
      assertThat(goal.steps.map { it.title }).containsExactly( // steps are returned in correct sequence
        "Book course",
        "Pass exam",
        "The new step",
      )
    }
  }

  @Test
  fun `should fail to create Goal given no Steps`() {
    // Given
    val goalReference = UUID.randomUUID()
    val steps = emptyList<Step>()

    // When
    val exception: InvalidGoalException = catchThrowableOfType(
      {
        Goal(
          reference = goalReference,
          title = "Improve woodworking skills",
          reviewDate = LocalDate.now().plusMonths(6),
          status = ACTIVE,
          createdBy = "bjones_gen",
          createdByDisplayName = "Barry Jones",
          createdAt = Instant.now(),
          lastUpdatedBy = "bjones_gen",
          lastUpdatedByDisplayName = "Barry Jones",
          lastUpdatedAt = Instant.now(),
          steps = steps,
        )
      },
      InvalidGoalException::class.java,
    )

    // Then
    assertThat(exception.message).isEqualTo("Cannot create Goal with reference [$goalReference]. At least one Step is required.")
  }
}
