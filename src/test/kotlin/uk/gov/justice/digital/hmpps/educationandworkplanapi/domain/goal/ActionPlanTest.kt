package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ActionPlanTest {

  @Test
  fun `should create ActionPlan given Goals out of sequence`() {
    // Given
    val goal1 = aValidGoal(reviewDate = LocalDate.now().plusMonths(1))
    val goal2 = aValidGoal(reviewDate = LocalDate.now().plusMonths(3))
    val goal3 = aValidGoal(reviewDate = LocalDate.now().plusMonths(6))

    // When
    val goal = aValidActionPlan(goals = mutableListOf(goal2, goal3, goal1))

    // Then
    assertThat(goal.goals).containsExactly(
      goal1,
      goal2,
      goal3,
    )
  }

  @Test
  fun `should add Goal and maintain Goal order`() {
    // Given
    val goal1 = aValidGoal(reviewDate = LocalDate.now().plusMonths(1))
    val goal2 = aValidGoal(reviewDate = LocalDate.now().plusMonths(3))
    val goal3 = aValidGoal(reviewDate = LocalDate.now().plusMonths(6))
    val goal = aValidActionPlan(goals = mutableListOf(goal1, goal3))

    // When
    goal.addGoal(goal2)

    // Then
    assertThat(goal.goals).containsExactly(
      goal1,
      goal2,
      goal3,
    )
  }
}
