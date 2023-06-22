package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.GoalService

@Deprecated("Remove this test when we have integration tests that hit controller endpoints and therefore exercise the GoalService")
class GoalServiceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var goalService: GoalService

  @Test
  @Transactional
  fun `should create new goal given action plan does not exist`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val goal = aValidGoal()

    // When
    goalService.createGoal(goal, prisonNumber)

    // Then
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actionPlan!!.goals).hasSize(1)
  }

  @Test
  @Transactional
  fun `should create new goal given action plan already exists`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    val goal = aValidGoal(
      title = "Goal 1",
    )
    goalService.createGoal(goal, prisonNumber)

    val newGoal = aValidGoal(
      title = "Goal 2",
    )

    // When
    goalService.createGoal(newGoal, prisonNumber)

    // Then
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val actionPlan = actionPlanRepository.findByPrisonNumber(prisonNumber)
    assertThat(actionPlan!!.goals).flatMap({ it.title }).containsOnly("Goal 1", "Goal 2")
  }
}
