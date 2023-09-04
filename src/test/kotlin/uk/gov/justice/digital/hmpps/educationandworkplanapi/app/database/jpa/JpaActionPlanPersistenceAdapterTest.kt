package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidActionPlanSummaryProjection
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.ActionPlanEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlan
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidActionPlanSummary
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateActionPlanDto

@ExtendWith(MockitoExtension::class)
class JpaActionPlanPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaActionPlanPersistenceAdapter

  @Mock
  private lateinit var actionPlanRepository: ActionPlanRepository

  @Mock
  private lateinit var actionPlanMapper: ActionPlanEntityMapper

  @Test
  fun `should save Action Plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlanDomain = aValidActionPlan(prisonNumber = prisonNumber)
    val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber)
    given(actionPlanMapper.fromDtoToEntity(any())).willReturn(actionPlanEntity)
    given(actionPlanRepository.save(any<ActionPlanEntity>())).willReturn(actionPlanEntity)
    given(actionPlanMapper.fromEntityToDomain(any())).willReturn(actionPlanDomain)

    val createActionPlanDto = aValidCreateActionPlanDto(prisonNumber = prisonNumber)

    // When
    val actual = persistenceAdapter.createActionPlan(createActionPlanDto)

    // Then
    assertThat(actual).isEqualTo(actionPlanDomain)
    verify(actionPlanRepository).save(actionPlanEntity)
    verify(actionPlanMapper).fromDtoToEntity(createActionPlanDto)
    verify(actionPlanMapper).fromEntityToDomain(actionPlanEntity)
  }

  @Test
  fun `should retrieve Action Plan`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber)
    val expectedActionPlanDomain = aValidActionPlan(prisonNumber = prisonNumber)
    given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)
    given(actionPlanMapper.fromEntityToDomain(any())).willReturn(expectedActionPlanDomain)

    // When
    val actual = persistenceAdapter.getActionPlan(prisonNumber)

    // Then
    assertThat(actual).isEqualTo(expectedActionPlanDomain)
    verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
    verify(actionPlanMapper).fromEntityToDomain(actionPlanEntity)
  }

  @Test
  fun `should retrieve Action Plan Summaries`() {
    // Given
    val prisonNumber = aValidPrisonNumber()
    val prisonNumbers = listOf(prisonNumber)
    val actionPlanSummaryProjections = listOf(aValidActionPlanSummaryProjection(prisonNumber = prisonNumber))
    val expectedActionPlanSummaries = listOf(aValidActionPlanSummary(prisonNumber = prisonNumber))
    given(actionPlanRepository.findByPrisonNumberIn(any())).willReturn(actionPlanSummaryProjections)
    given(actionPlanMapper.fromEntitySummariesToDomainSummaries(any())).willReturn(expectedActionPlanSummaries)

    // When
    val actual = persistenceAdapter.getActionPlanSummaries(prisonNumbers)

    // Then
    assertThat(actual).isEqualTo(expectedActionPlanSummaries)
    verify(actionPlanRepository).findByPrisonNumberIn(prisonNumbers)
    verify(actionPlanMapper).fromEntitySummariesToDomainSummaries(actionPlanSummaryProjections)
  }
}
