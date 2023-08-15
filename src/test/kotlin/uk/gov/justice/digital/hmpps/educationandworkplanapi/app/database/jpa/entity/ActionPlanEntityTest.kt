package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.util.UUID

class ActionPlanEntityTest {

  @Test
  fun `should create new action plan for prisoner`() {
    // Given
    val prisonNumber = aValidPrisonNumber()

    // When
    val actual = ActionPlanEntity.newActionPlanForPrisoner(reference = UUID.randomUUID(), prisonNumber = prisonNumber, reviewDate = null)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .isForPrisonNumber(prisonNumber)
      .hasNoGoalsSet()
  }

  @Test
  fun `should get goal by reference given goal with reference exists`() {
    // Given
    val goalReference = UUID.randomUUID()
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
    )
    val actionPlanEntity = aValidActionPlanEntity(
      goals = mutableListOf(
        aValidGoalEntity(),
        goalEntity,
        aValidGoalEntity(),
      ),
    )

    // When
    val actual = actionPlanEntity.getGoalByReference(goalReference)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(goalEntity)
  }

  @Test
  fun `should not get goal by reference given goal with reference does not exist`() {
    // Given
    val goalReference = UUID.randomUUID()
    val actionPlanEntity = aValidActionPlanEntity(
      goals = mutableListOf(
        aValidGoalEntity(),
      ),
    )

    // When
    val actual = actionPlanEntity.getGoalByReference(goalReference)

    // Then
    assertThat(actual).isNull()
  }

  @Test
  fun `should add goal`() {
    // Given
    val actionPlanEntity = aValidActionPlanEntity(
      goals = mutableListOf(),
    )

    val goalEntity = aValidGoalEntity()

    // When
    val actual = actionPlanEntity.addGoal(goalEntity)

    // Then
    Assertions.assertThat(actual.goals).hasSize(1)
  }
}
