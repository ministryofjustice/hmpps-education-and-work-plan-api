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
