package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalService
import java.util.UUID

@Deprecated("Remove this test when we have integration tests that hit controller endpoints and therefore exercise the GoalService")
class GoalServiceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var goalService: GoalService

  @Test
  @Transactional
  fun `should save new goal given action plan does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goal = aValidGoal()

    // When
    goalService.saveGoal(goal, prisonNumber)

    // Then
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actionPlan!!.goals).hasSize(1)
  }

  @Test
  @Transactional
  fun `should save new goal given action plan already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val goal = aValidGoal(
      title = "Goal 1",
    )
    goalService.saveGoal(goal, prisonNumber)

    val newGoal = aValidGoal(
      title = "Goal 2",
    )

    // When
    goalService.saveGoal(newGoal, prisonNumber)

    // Then
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actionPlan!!.goals).flatMap({ it.title }).containsOnly("Goal 1", "Goal 2")
  }

  @Test
  @Transactional
  fun `should save updated goal given goal already exists in action plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val reference = UUID.randomUUID()

    val goal = aValidGoal(
      title = "Original goal title",
      reference = reference,
    )
    goalService.saveGoal(goal, prisonNumber)

    val updatedGoal = aValidGoal(
      title = "Updated goal title",
      reference = reference,
    )

    // When
    goalService.saveGoal(updatedGoal, prisonNumber)

    // Then
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actionPlan!!.goals).flatMap({ it.title }).containsOnly("Updated goal title")
  }
}
