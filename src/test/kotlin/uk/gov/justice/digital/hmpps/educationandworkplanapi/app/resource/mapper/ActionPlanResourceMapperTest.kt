package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper

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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.aValidGoalResponse

@ExtendWith(MockitoExtension::class)
internal class ActionPlanResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: ActionPlanResourceMapperImpl

  @Mock
  private lateinit var goalMapper: GoalResourceMapper

  @Test
  fun `should map from model to domain`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val request = aValidCreateActionPlanRequest()
    val expectedGoal = aValidGoal()
    val expectedActionPlan = aValidActionPlan(
      prisonNumber = prisonNumber,
      reviewDate = request.reviewDate,
      goals = listOf(expectedGoal),
    )
    given(goalMapper.fromModelToDomain(any<CreateGoalRequest>())).willReturn(expectedGoal)

    // When
    val actual = mapper.fromModelToDomain(prisonNumber, request)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("reference").isEqualTo(expectedActionPlan)
    verify(goalMapper).fromModelToDomain(request.goals[0])
  }

  @Test
  fun `should map from domain to model`() {
    // Given
    val actionPlan = aValidActionPlan()
    val expectedGoal = aValidGoalResponse()
    val expectedActionPlan = aValidActionPlanResponse(
      reference = actionPlan.reference,
      prisonNumber = actionPlan.prisonNumber,
      reviewDate = actionPlan.reviewDate,
      goals = mutableListOf(expectedGoal),
    )
    given(goalMapper.fromDomainToModel(any())).willReturn(expectedGoal)

    // When
    val actual = mapper.fromDomainToModel(actionPlan)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedActionPlan)
    verify(goalMapper).fromDomainToModel(actionPlan.goals[0])
  }
}
