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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalCategory as EntityCategory
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalStatus as EntityStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalCategory as DomainCategory
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus as DomainStatus

@ExtendWith(MockitoExtension::class)
class GoalEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: GoalEntityMapperImpl

  @Mock
  private lateinit var stepMapper: StepEntityMapper

  @Test
  fun `should map from domain to entity`() {
    // Given
    val domainStep = aValidStep()
    val domainGoal = aValidGoal(
      reference = UUID.randomUUID(),
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      category = DomainCategory.PERSONAL_DEVELOPMENT,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(domainStep),
    )

    val expectedEntityStep = aValidStepEntity()
    given(stepMapper.fromDomainToEntity(any())).willReturn(expectedEntityStep)

    val expected = aValidGoalEntity(
      id = null,
      reference = domainGoal.reference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      category = EntityCategory.PERSONAL_DEVELOPMENT,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(expectedEntityStep),
    )

    // When
    val actual = mapper.fromDomainToEntity(domainGoal)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromDomainToEntity(domainStep)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val entityStep = aValidStepEntity()
    val entityGoal = aValidGoalEntity(
      id = UUID.randomUUID(),
      reference = UUID.randomUUID(),
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      category = EntityCategory.PERSONAL_DEVELOPMENT,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(entityStep),
    )

    val domainStep = aValidStep()
    given(stepMapper.fromEntityToDomain(any())).willReturn(domainStep)

    val expected = aValidGoal(
      reference = entityGoal.reference!!,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      category = DomainCategory.PERSONAL_DEVELOPMENT,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(domainStep),
    )

    // When
    val actual = mapper.fromEntityToDomain(entityGoal)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromEntityToDomain(entityStep)
  }
}
