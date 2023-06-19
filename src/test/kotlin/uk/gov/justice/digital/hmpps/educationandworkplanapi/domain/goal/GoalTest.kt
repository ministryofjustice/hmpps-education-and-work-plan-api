package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalCategory.RESETTLEMENT
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class GoalTest {

  @Test
  fun `should create Goal given Steps out of sequence`() {
    // Given
    val step1 = aValidStep(sequenceNumber = 1)
    val step2 = aValidStep(sequenceNumber = 2)
    val step3 = aValidStep(sequenceNumber = 3)

    // When
    val goal = aValidGoal(steps = mutableListOf(step2, step3, step1))

    // Then
    assertThat(goal.steps).containsExactly(
      step1,
      step2,
      step3,
    )
  }

  @Test
  fun `should add Step and maintain Step order`() {
    // Given
    val step1 = aValidStep(sequenceNumber = 1)
    val step2 = aValidStep(sequenceNumber = 2)
    val step3 = aValidStep(sequenceNumber = 3)
    val goal = aValidGoal(steps = mutableListOf(step1, step3))

    // When
    goal.addStep(step2)

    // Then
    assertThat(goal.steps).containsExactly(
      step1,
      step2,
      step3,
    )
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
          category = RESETTLEMENT,
          status = ACTIVE,
          createdBy = "",
          createdAt = Instant.now(),
          lastUpdatedBy = "",
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
