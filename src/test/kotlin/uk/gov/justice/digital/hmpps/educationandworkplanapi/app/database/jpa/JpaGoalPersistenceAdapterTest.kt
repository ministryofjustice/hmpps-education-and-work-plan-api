package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.domain.personallearningplan.ActionPlanNotFoundException
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.ReasonToArchiveGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidArchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUnarchiveGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateGoalDto
import uk.gov.justice.digital.hmpps.domain.randomValidPrisonNumber
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidActionPlanEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.GoalRepository
import java.util.*

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
      val prisonNumber = randomValidPrisonNumber()
      val createGoalDtos = listOf(aValidCreateGoalDto())
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val exception = assertThrows(ActionPlanNotFoundException::class.java) {
        persistenceAdapter.createGoals(
          prisonNumber,
          createGoalDtos,
        )
      }

      // Then
      assertThat(exception).hasMessage("ActionPlan for prisoner [$prisonNumber] not found")
      verifyNoInteractions(goalMapper)
    }

    @Test
    fun `should create goals given action plan already exists`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity =
        aValidGoalEntity(reference = reference, title = "Original goal title", createdBy = "USER1", updatedBy = "USER1")
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val persistedGoalEntity =
        aValidGoalEntity(reference = reference, title = "Updated goal title", createdBy = "USER1", updatedBy = "USER2")
      given(goalRepository.saveAndFlush(any<GoalEntity>())).willReturn(persistedGoalEntity)

      val expectedDomainGoal =
        aValidGoal(reference = reference, title = "Updated goal title", createdBy = "USER1", lastUpdatedBy = "USER2")
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
      val prisonNumber = randomValidPrisonNumber()
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
      val prisonNumber = randomValidPrisonNumber()
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

  @Nested
  inner class ArchiveGoal {
    @Test
    fun `should return null if existing goal could not be found`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(reference = UUID.randomUUID())
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val archiveGoalDto = aValidArchiveGoalDto(reference = reference)

      // When
      val actual = persistenceAdapter.archiveGoal(prisonNumber, archiveGoalDto)

      // Then
      assertThat(actual).isNull()
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(goalMapper)
      verifyNoMoreInteractions(goalRepository)
    }

    @Test
    fun `should archive the goal`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(reference = reference)
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)
      val persistedGoalEntity =
        aValidGoalEntity(
          reference = reference,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus.ARCHIVED,
          archiveReason = uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ReasonToArchiveGoal.OTHER,
          archiveReasonOther = "Foo",
        )
      given(goalRepository.saveAndFlush(any<GoalEntity>())).willReturn(persistedGoalEntity)

      val expectedDomainGoal = aValidGoal(
        reference = reference,
        status = GoalStatus.ARCHIVED,
        archiveReason = ReasonToArchiveGoal.OTHER,
        archiveReasonOther = "Foo",
      )
      given(goalMapper.fromEntityToDomain(any())).willReturn(expectedDomainGoal)
      given(goalMapper.archiveReasonFromDomainToEntity(ReasonToArchiveGoal.OTHER)).willReturn(uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ReasonToArchiveGoal.OTHER)

      // When
      val actual = persistenceAdapter.archiveGoal(
        prisonNumber,
        aValidArchiveGoalDto(reference = reference, reason = ReasonToArchiveGoal.OTHER, reasonOther = "Foo"),
      )

      // Then
      assertThat(actual).isNotNull
      assertThat(actual!!.status).isEqualTo(GoalStatus.ARCHIVED)
      assertThat(actual.archiveReason).isEqualTo(ReasonToArchiveGoal.OTHER)
      assertThat(actual.archiveReasonOther).isEqualTo("Foo")

      val captor = argumentCaptor<GoalEntity>()
      verify(goalRepository).saveAndFlush(captor.capture())
      assertThat(captor.firstValue.status).isEqualTo(uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus.ARCHIVED)
      assertThat(captor.firstValue.archiveReason).isEqualTo(uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.ReasonToArchiveGoal.OTHER)
      assertThat(captor.firstValue.archiveReasonOther).isEqualTo("Foo")
    }
  }

  @Nested
  inner class UnarchiveGoal {
    @Test
    fun `should return null if existing goal could not be found`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(
        reference = UUID.randomUUID(),
        status = uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus.ARCHIVED,
      )
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val unarchiveGoalDto = aValidUnarchiveGoalDto(reference = reference)

      // When
      val actual = persistenceAdapter.unarchiveGoal(prisonNumber, unarchiveGoalDto)

      // Then
      assertThat(actual).isNull()
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(goalMapper)
      verifyNoMoreInteractions(goalRepository)
    }

    @Test
    fun `should unarchive the goal`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()
      val reference = UUID.randomUUID()

      val goalEntity = aValidGoalEntity(
        reference = reference,
        status = uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus.ARCHIVED,
      )
      val actionPlanEntity = aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)
      val persistedGoalEntity =
        aValidGoalEntity(
          reference = reference,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus.ACTIVE,
        )
      given(goalRepository.saveAndFlush(any<GoalEntity>())).willReturn(persistedGoalEntity)

      val expectedDomainGoal = aValidGoal(
        reference = reference,
        status = GoalStatus.ACTIVE,
        archiveReason = null,
        archiveReasonOther = null,
      )
      given(goalMapper.fromEntityToDomain(any())).willReturn(expectedDomainGoal)

      // When
      val actual = persistenceAdapter.unarchiveGoal(prisonNumber, aValidUnarchiveGoalDto(reference = reference))

      // Then
      assertThat(actual).isNotNull
      assertThat(actual!!.status).isEqualTo(GoalStatus.ACTIVE)
      assertThat(actual.archiveReason).isNull()
      assertThat(actual.archiveReasonOther).isNull()

      val captor = argumentCaptor<GoalEntity>()
      verify(goalRepository).saveAndFlush(captor.capture())
      assertThat(captor.firstValue.status).isEqualTo(uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus.ACTIVE)
      assertThat(captor.firstValue.archiveReason).isNull()
      assertThat(captor.firstValue.archiveReasonOther).isNull()
    }
  }

  @Nested
  inner class GetGoals {
    @Test
    fun `should get goals gived goals exists in prisoners action plan`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      val goalEntity1 = aValidGoalEntity()
      val goalEntity2 = aValidGoalEntity()
      val actionPlanEntity =
        aValidActionPlanEntity(prisonNumber = prisonNumber, goals = listOf(goalEntity1, goalEntity2))
      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(actionPlanEntity)

      val expectedDomainGoal1 = aValidGoal(reference = goalEntity1.reference)
      val expectedDomainGoal2 = aValidGoal(reference = goalEntity2.reference)
      given(goalMapper.fromEntityToDomain(goalEntity1)).willReturn(expectedDomainGoal1)
      given(goalMapper.fromEntityToDomain(goalEntity2)).willReturn(expectedDomainGoal2)

      // When
      val actual = persistenceAdapter.getGoals(prisonNumber)

      // Then
      assertThat(actual).isEqualTo(listOf(expectedDomainGoal1, expectedDomainGoal2))
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verify(goalMapper).fromEntityToDomain(goalEntity1)
      verify(goalMapper).fromEntityToDomain(goalEntity2)
    }

    @Test
    fun `should not get goal given prisoners action plan does not exist`() {
      // Given
      val prisonNumber = randomValidPrisonNumber()

      given(actionPlanRepository.findByPrisonNumber(any())).willReturn(null)

      // When
      val actual = persistenceAdapter.getGoals(prisonNumber)

      // Then
      assertThat(actual).isNull()
      verify(actionPlanRepository).findByPrisonNumber(prisonNumber)
      verifyNoInteractions(goalMapper)
    }
  }
}
