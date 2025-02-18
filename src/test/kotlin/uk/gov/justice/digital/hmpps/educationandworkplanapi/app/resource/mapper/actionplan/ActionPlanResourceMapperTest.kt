package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource.mapper.actionplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.domain.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.anotherValidPrisonNumber
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Goal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlanSummary
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateActionPlanDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateGoalRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidActionPlanResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidActionPlanSummaryResponse
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidCreateActionPlanRequest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan.aValidGoalResponse

@ExtendWith(MockitoExtension::class)
internal class ActionPlanResourceMapperTest {
  @InjectMocks
  private lateinit var mapper: ActionPlanResourceMapper

  @Mock
  private lateinit var goalMapper: GoalResourceMapper

  @Test
  fun `should map from model to DTO`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val request = aValidCreateActionPlanRequest()
    val expectedCreateGoalDto = aValidCreateGoalDto()
    val expectedCreateActionPlanDto = aValidCreateActionPlanDto(
      prisonNumber = prisonNumber,
      goals = listOf(expectedCreateGoalDto),
    )
    given(goalMapper.fromModelToDto(any<CreateGoalRequest>())).willReturn(expectedCreateGoalDto)

    // When
    val actual = mapper.fromModelToDto(prisonNumber, request)

    // Then
    assertThat(actual).isEqualTo(expectedCreateActionPlanDto)
    verify(goalMapper).fromModelToDto(request.goals[0])
  }

  @Test
  fun `should map from domain to model`() {
    // Given
    val actionPlan = aValidActionPlan()
    val expectedGoal = aValidGoalResponse()
    val expectedActionPlan = aValidActionPlanResponse(
      reference = actionPlan.reference,
      prisonNumber = actionPlan.prisonNumber,
      goals = mutableListOf(expectedGoal),
    )
    given(goalMapper.fromDomainToModel(any<Goal>())).willReturn(expectedGoal)

    // When
    val actual = mapper.fromDomainToModel(actionPlan)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expectedActionPlan)
    verify(goalMapper).fromDomainToModel(actionPlan.goals[0])
  }

  @Test
  fun `should map from domain summaries to model summaries`() {
    // Given
    val summary1 = aValidActionPlanSummary(prisonNumber = aValidPrisonNumber())
    val summary2 = aValidActionPlanSummary(prisonNumber = anotherValidPrisonNumber())
    val expected = listOf(
      aValidActionPlanSummaryResponse(
        prisonNumber = summary1.prisonNumber,
        reference = summary1.reference,
      ),
      aValidActionPlanSummaryResponse(
        prisonNumber = summary2.prisonNumber,
        reference = summary2.reference,
      ),
    )

    // When
    val actual = mapper.fromDomainToModel(listOf(summary1, summary2))

    // Then
    assertThat(actual).hasSize(2)
    assertThat(actual).isEqualTo(expected)
  }
}
