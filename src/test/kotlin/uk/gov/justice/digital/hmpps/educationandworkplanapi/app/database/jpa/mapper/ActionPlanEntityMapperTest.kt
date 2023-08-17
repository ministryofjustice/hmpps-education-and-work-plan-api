package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ReviewDateCategory as ReviewDateCategoryEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ReviewDateCategory as ReviewDateCategoryDomain

@ExtendWith(MockitoExtension::class)
class ActionPlanEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: ActionPlanEntityMapperImpl

  @Mock
  private lateinit var goalMapper: GoalEntityMapper

  @Test
  fun `should map from domain to entity`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlan = aValidActionPlan(
      prisonNumber = prisonNumber,
    )
    val expectedGoalEntity = aValidGoalEntity()
    val expected = aValidActionPlanEntity(
      reference = actionPlan.reference,
      prisonNumber = prisonNumber,
      reviewDateCategory = ReviewDateCategoryEntity.SPECIFIC_DATE,
      reviewDate = actionPlan.reviewDate,
      goals = mutableListOf(expectedGoalEntity),
    )
    given(goalMapper.fromDomainToEntity(any())).willReturn(expectedGoalEntity)

    // When
    val actual = mapper.fromDomainToEntity(actionPlan)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("id").isEqualTo(expected)
    verify(goalMapper).fromDomainToEntity(actionPlan.goals[0])
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber)
    val goalDomain = aValidGoal()
    given(goalMapper.fromEntityToDomain(any())).willReturn(goalDomain)
    val expected = aValidActionPlan(
      reference = actionPlanEntity.reference!!,
      prisonNumber = prisonNumber,
      reviewDateCategory = ReviewDateCategoryDomain.SPECIFIC_DATE,
      reviewDate = actionPlanEntity.reviewDate,
      goals = mutableListOf(goalDomain),
    )

    // When
    val actual = mapper.fromEntityToDomain(actionPlanEntity)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(goalMapper).fromEntityToDomain(actionPlanEntity.goals!![0])
  }
}
