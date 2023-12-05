package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.GoalRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidUpdateGoalDto
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class JpaGoalPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaGoalPersistenceAdapter

  @Mock
  private lateinit var goalRepository: GoalRepository

  @Mock
  private lateinit var actionPlanRepository: ActionPlanRepository

  @Mock
  private lateinit var goalMapper: GoalEntityMapper

  @Nested
  inner class CreateGoals {
    @Test
    fun `should fail to create goals given action plan does not already exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val createGoalDtos = listOf(aValidCreateGoalDto())
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val exception = catchThrowableOfType(
        { persistenceAdapter.createGoals(prisonNumber, createGoalDtos) },
        ActionPlanNotFoundException::class.java,
      )

      // Then
      assertThat(exception).hasMessage("Unable to find ActionPlan for prisoner [$prisonNumber]")
      verifyNoInteractions(goalMapper)
    }

    @Test
    fun `should create goals given action plan already exists`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference1 = UUID.randomUUID()
      val reference2 = UUID.randomUUID()

      val domainGoal1 = aValidGoal(
        reference = reference1,
      )
      val domainGoal2 = aValidGoal(
        reference = reference2,
      )

      val createGoalDto1 = aValidCreateGoalDto(title = "Goal 1")
      val createGoalDto2 = aValidCreateGoalDto(title = "Goal 2")
      val createGoalDtos = listOf(createGoalDto1, createGoalDto2)

      val initialActionPlan = aValidActionPlanEntity(
        prisonNumber = prisonNumber,
        goals = emptyList(),
      )

      val entityGoal1 = aValidGoalEntity(
        reference = reference1,
      )
      val entityGoal2 = aValidGoalEntity(
        reference = reference2,
      )
      val actionPlanEntity = aValidActionPlanEntity(
        prisonNumber = prisonNumber,
        goals = mutableListOf(entityGoal1, entityGoal2),
      )
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(initialActionPlan)
      given(goalMapper.fromDtoToEntity(any())).willReturn(entityGoal1, entityGoal2)
      given(actionPlanRepository.save(any<ActionPlanEntity>())).willReturn(actionPlanEntity)
      given(goalMapper.fromEntityToDomain(any())).willReturn(domainGoal1, domainGoal2)

      // When
      val actual = persistenceAdapter.createGoals(prisonNumber, createGoalDtos)

      // Then
      assertThat(actual).containsExactlyInAnyOrder(domainGoal1, domainGoal2)
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verify(goalMapper).fromDtoToEntity(createGoalDto1)
      verify(goalMapper).fromDtoToEntity(createGoalDto2)
      verify(goalMapper).fromEntityToDomain(entityGoal1)
      verify(goalMapper).fromEntityToDomain(entityGoal2)
    }
  }

  @Nested
  inner class GetGoal {
    @Test
    fun `should get goal given goal exists in prisoners action plan`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(reference = reference)
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val expectedDomainGoal = aValidGoal(reference = reference)
      given(goalMapper.fromEntityToDomain(any())).willReturn(expectedDomainGoal)

      // When
      val actual = persistenceAdapter.getGoal(prisonNumber, reference)

      // Then
      assertThat(actual).isEqualTo(expectedDomainGoal)
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verify(goalMapper).fromEntityToDomain(goalEntity)
    }

    @Test
    fun `should not get goal given goal does not exist in prisoners action plan`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(reference = UUID.randomUUID())
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      // When
      val actual = persistenceAdapter.getGoal(prisonNumber, reference)

      // Then
      assertThat(actual).isNull()
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(goalMapper)
    }

    @Test
    fun `should not get goal given prisoners action plan does not exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val actual = persistenceAdapter.getGoal(prisonNumber, reference)

      // Then
      assertThat(actual).isNull()
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(goalMapper)
    }
  }

  @Nested
  inner class UpdateGoal {
    @Test
    fun `should update goal given goal exists in prisoners action plan`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(reference = reference, title = "Original goal title", createdBy = "USER1", updatedBy = "USER1")
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val persistedGoalEntity = aValidGoalEntity(reference = reference, title = "Updated goal title", createdBy = "USER1", updatedBy = "USER2")
      given(goalRepository.saveAndFlush(any<GoalEntity>())).willReturn(persistedGoalEntity)

      val expectedDomainGoal = aValidGoal(reference = reference, title = "Updated goal title", createdBy = "USER1", lastUpdatedBy = "USER2")
      given(goalMapper.fromEntityToDomain(any())).willReturn(expectedDomainGoal)

      val goalWithProposedUpdates = aValidUpdateGoalDto(reference = reference, title = "Updated goal title")

      // When
      val actual = persistenceAdapter.updateGoal(prisonNumber, goalWithProposedUpdates)

      // Then
      assertThat(actual).isEqualTo(expectedDomainGoal)
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verify(goalRepository).saveAndFlush(goalEntity)
      verify(goalMapper).updateEntityFromDto(goalEntity, goalWithProposedUpdates)
      verify(goalMapper).fromEntityToDomain(persistedGoalEntity)
    }

    @Test
    fun `should not update goal given goal does not exist in prisoners action plan`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(reference = UUID.randomUUID())
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val goalWithProposedUpdates = aValidUpdateGoalDto(reference = reference)

      // When
      val actual = persistenceAdapter.updateGoal(prisonNumber, goalWithProposedUpdates)

      // Then
      assertThat(actual).isNull()
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(goalMapper)
    }

    @Test
    fun `should not update goal given prisoners action plan does not exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(null)

      val goalWithProposedUpdates = aValidUpdateGoalDto(reference = reference)

      // When
      val actual = persistenceAdapter.updateGoal(prisonNumber, goalWithProposedUpdates)

      // Then
      assertThat(actual).isNull()
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(goalMapper)
    }
  }
}
