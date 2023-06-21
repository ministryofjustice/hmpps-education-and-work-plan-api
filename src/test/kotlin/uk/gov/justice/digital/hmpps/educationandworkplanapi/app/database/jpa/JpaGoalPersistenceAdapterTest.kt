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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.GoalEntityMapper
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository.GoalRepository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal

@ExtendWith(MockitoExtension::class)
class JpaGoalPersistenceAdapterTest {

  @InjectMocks
  private lateinit var persistenceAdapter: JpaGoalPersistenceAdapter

  @Mock
  private lateinit var goalRepository: GoalRepository

  @Mock
  private lateinit var goalMapper: GoalEntityMapper

  @Test
  fun `should save goal`() {
    // Given
    val domainGoal = aValidGoal()

    val entityGoal = aValidGoalEntity()
    given(goalMapper.fromDomainToEntity(any())).willReturn(entityGoal)
    given(goalRepository.save(any<GoalEntity>())).willReturn(entityGoal)
    given(goalMapper.fromEntityToDomain(any())).willReturn(domainGoal)

    val prisonNumber = aValidPrisonNumber()

    // When
    val actual = persistenceAdapter.saveGoal(domainGoal, prisonNumber)

    // Then
    assertThat(actual).isEqualTo(domainGoal)
    verify(goalMapper).fromDomainToEntity(domainGoal)
    verify(goalRepository).save(entityGoal)
    verify(goalMapper).fromEntityToDomain(entityGoal)
  }
}
