package uk.gov.justice.digital.hmpps.educationandworkplanapi.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.service.ActionPlanService

@Deprecated("A temporary test until we have integration tests which use the relevant controller endpoints")
class ActionPlanServiceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var actionPlanService: ActionPlanService

  @Test
  @Transactional
  fun `should get action plan summaries`() {
    // Given
    val prisonNumber1 = aValidPrisonNumber()
    val prisonNumber2 = anotherValidPrisonNumber()
    val actionPlan1 = aValidActionPlan(prisonNumber = prisonNumber1)
    val actionPlan2 = aValidActionPlan(prisonNumber = prisonNumber2)
    actionPlanService.createActionPlan(actionPlan1)
    actionPlanService.createActionPlan(actionPlan2)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val expected = listOf(
      aValidActionPlanSummary(
        reference = actionPlan1.reference,
        prisonNumber = prisonNumber1,
        reviewDate = actionPlan1.reviewDate,
      ),
      aValidActionPlanSummary(
        reference = actionPlan2.reference,
        prisonNumber = prisonNumber2,
        reviewDate = actionPlan2.reviewDate,
      ),
    )

    // When
    val actionPlanSummaries = actionPlanService.getActionPlanSummaries(listOf(prisonNumber1, prisonNumber2))

    // Then
    assertThat(actionPlanSummaries).hasSize(2)
    assertThat(actionPlanSummaries).containsExactlyInAnyOrderElementsOf(expected)
  }

  @Test
  @Transactional
  fun `should get an empty collection when no action plans exist for given prisoners`() {
    // Given
    val prisonNumber1 = aValidPrisonNumber()
    val prisonNumber2 = anotherValidPrisonNumber()

    // When
    val actionPlanSummaries = actionPlanService.getActionPlanSummaries(listOf(prisonNumber1, prisonNumber2))

    // Then
    assertThat(actionPlanSummaries).isEmpty()
  }
}
