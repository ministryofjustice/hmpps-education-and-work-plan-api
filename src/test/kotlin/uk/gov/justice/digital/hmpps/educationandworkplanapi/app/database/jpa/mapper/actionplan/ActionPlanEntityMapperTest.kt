package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

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
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlan
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidActionPlanSummary
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateActionPlanDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidActionPlanSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat

@ExtendWith(MockitoExtension::class)
class ActionPlanEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: ActionPlanEntityMapperImpl

  @Mock
  private lateinit var goalMapper: GoalEntityMapper

  @Test
  fun `should map from DTO to entity`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlan = aValidActionPlan(
      prisonNumber = prisonNumber,
    )
    val expectedGoalEntity = aValidGoalEntity()
    val expected = aValidActionPlanEntity(
      reference = actionPlan.reference,
      prisonNumber = prisonNumber,
      reviewDate = actionPlan.reviewDate,
      goals = mutableListOf(expectedGoalEntity),
      // JPA managed fields - expect these all to be null, implying a new db entity
      id = null,
    )
    given(goalMapper.fromDtoToEntity(any())).willReturn(expectedGoalEntity)

    val createActionPlanDto = aValidCreateActionPlanDto()

    // When
    val actual = mapper.fromDtoToEntity(createActionPlanDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFields("reference")
      .isEqualTo(expected)
    verify(goalMapper).fromDtoToEntity(createActionPlanDto.goals[0])
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
      reviewDate = actionPlanEntity.reviewDate,
      goals = mutableListOf(goalDomain),
    )

    // When
    val actual = mapper.fromEntityToDomain(actionPlanEntity)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(goalMapper).fromEntityToDomain(actionPlanEntity.goals!![0])
  }

  @Test
  fun `should map from entity summaries to domain summaries`() {
    // Given
    val summaryProjection1 = aValidActionPlanSummaryProjection(prisonNumber = aValidPrisonNumber())
    val summaryProjection2 = aValidActionPlanSummaryProjection(prisonNumber = anotherValidPrisonNumber())
    val expected = listOf(
      aValidActionPlanSummary(
        prisonNumber = summaryProjection1.prisonNumber,
        reference = summaryProjection1.reference,
        reviewDate = summaryProjection1.reviewDate,
      ),
      aValidActionPlanSummary(
        prisonNumber = summaryProjection2.prisonNumber,
        reference = summaryProjection2.reference,
        reviewDate = summaryProjection2.reviewDate,
      ),
    )

    // When
    val actual = mapper.fromEntitySummariesToDomainSummaries(listOf(summaryProjection1, summaryProjection2))

    // Then
    assertThat(actual).hasSize(2)
    assertThat(actual).isEqualTo(expected)
  }
}
