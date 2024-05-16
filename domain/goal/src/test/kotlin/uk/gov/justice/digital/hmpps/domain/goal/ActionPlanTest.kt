package uk.gov.justice.digital.hmpps.domain.goal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class ActionPlanTest {

  @Test
  fun `should create ActionPlan given Goals out of sequence`() {
    // Given
    val goal1 = aValidGoal(createdAt = Instant.now())
    val goal2 = aValidGoal(createdAt = Instant.now().minusSeconds(10))
    val goal3 = aValidGoal(createdAt = Instant.now().minusSeconds(60))

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
    val goal1 = aValidGoal(createdAt = Instant.now())
    val goal2 = aValidGoal(createdAt = Instant.now().minusSeconds(10))
    val goal3 = aValidGoal(createdAt = Instant.now().minusSeconds(60))
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
