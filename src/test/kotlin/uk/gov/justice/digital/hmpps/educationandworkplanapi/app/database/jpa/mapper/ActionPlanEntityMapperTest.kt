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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal

@ExtendWith(MockitoExtension::class)
class ActionPlanEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: ActionPlanEntityMapperImpl

  @Mock
  private lateinit var goalMapper: GoalEntityMapper

  @Test
  fun `should map from entity to domain`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber)
    val goalDomain = aValidGoal()
    given(goalMapper.fromEntityToDomain(any())).willReturn(goalDomain)
    val expected = aValidActionPlan(prisonNumber = prisonNumber, goals = mutableListOf(goalDomain))

    // When
    val actual = mapper.fromEntityToDomain(actionPlanEntity)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(goalMapper).fromEntityToDomain(actionPlanEntity.goals!![0])
  }
}
