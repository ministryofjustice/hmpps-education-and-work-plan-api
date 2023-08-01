package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class JpaGoalPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaGoalPersistenceAdapter

  @Mock
  private lateinit var actionPlanRepository: ActionPlanRepository

  @Mock
  private lateinit var goalMapper: GoalEntityMapper

  @Nested
  inner class CreateGoal {
    @Test
    fun `should create goal given action plan does not already exist`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()
      val domainGoal = aValidGoal(
        reference = reference,
      )

      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(null)

      val entityGoal = aValidGoalEntity(
        reference = reference,
      )
      given(goalMapper.fromDomainToEntity(any())).willReturn(entityGoal)

      val actionPlanEntity = aValidActionPlanEntity(
        prisonNumber = prisonNumber,
        goals = mutableListOf(entityGoal),
      )
      given(actionPlanRepository.saveAndFlush(any<ActionPlanEntity>())).willReturn(actionPlanEntity)

      given(goalMapper.fromEntityToDomain(any())).willReturn(domainGoal)

      // When
      val actual = persistenceAdapter.createGoal(domainGoal, prisonNumber)

      // Then
      assertThat(actual).isEqualTo(domainGoal)
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verify(goalMapper).fromDomainToEntity(domainGoal)
      verify(goalMapper).fromEntityToDomain(entityGoal)
    }

    @Test
    fun `should create goal given action plan already exists`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()
      val domainGoal = aValidGoal(
        reference = reference,
      )

      val initialActionPlan = aValidActionPlanEntity(
        prisonNumber = prisonNumber,
        goals = mutableListOf(),
      )
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(initialActionPlan)

      val entityGoal = aValidGoalEntity(
        reference = reference,
      )
      given(goalMapper.fromDomainToEntity(any())).willReturn(entityGoal)

      val actionPlanEntity = aValidActionPlanEntity(
        prisonNumber = prisonNumber,
        goals = mutableListOf(entityGoal),
      )
      given(actionPlanRepository.saveAndFlush(any<ActionPlanEntity>())).willReturn(actionPlanEntity)

      given(goalMapper.fromEntityToDomain(any())).willReturn(domainGoal)

      // When
      val actual = persistenceAdapter.createGoal(domainGoal, prisonNumber)

      // Then
      assertThat(actual).isEqualTo(domainGoal)
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verify(goalMapper).fromDomainToEntity(domainGoal)
      verify(goalMapper).fromEntityToDomain(entityGoal)
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

      val goalEntity = aValidGoalEntity(reference = reference, title = "Original goal title")
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val goalWithProposedUpdates = aValidGoal(reference = reference, title = "Updated goal title")

      val expectedDomainGoal = aValidGoal(reference = reference, title = "Updated goal title")
      given(goalMapper.fromEntityToDomain(any())).willReturn(expectedDomainGoal)

      // When
      val actual = persistenceAdapter.updateGoal(prisonNumber, reference, goalWithProposedUpdates)

      // Then
      assertThat(actual).isEqualTo(expectedDomainGoal)
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verify(goalMapper).updateEntityFromDomain(goalEntity, goalWithProposedUpdates)
      verify(goalMapper).fromEntityToDomain(goalEntity)
    }

    @Test
    fun `should not update goal given goal does not exist in prisoners action plan`() {
      // Given
      val prisonNumber = aValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(reference = UUID.randomUUID())
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val goalWithProposedUpdates = aValidGoal()

      // When
      val actual = persistenceAdapter.updateGoal(prisonNumber, reference, goalWithProposedUpdates)

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

      val goalWithProposedUpdates = aValidGoal()

      // When
      val actual = persistenceAdapter.updateGoal(prisonNumber, reference, goalWithProposedUpdates)

      // Then
      assertThat(actual).isNull()
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(goalMapper)
    }
  }
}
