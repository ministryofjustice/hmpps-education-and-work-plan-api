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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import java.time.Instant
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
    val reviewDate = LocalDate.now().plusMonths(6)

    val domainStep = aValidStep()
    val domainGoal = aValidGoal(
      reference = UUID.randomUUID(),
      title = "Improve communication skills",
      reviewDate = reviewDate,
      category = DomainCategory.PERSONAL_DEVELOPMENT,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(domainStep),
      createdBy = "a.user.id",
      createdAt = Instant.now(),
      lastUpdatedBy = "another.user.id",
      lastUpdatedAt = Instant.now(),
    )

    val expectedEntityStep = aValidStepEntity()
    given(stepMapper.fromDomainToEntity(any())).willReturn(expectedEntityStep)

    val expected = aValidGoalEntity(
      reference = domainGoal.reference,
      title = "Improve communication skills",
      reviewDate = reviewDate,
      category = EntityCategory.PERSONAL_DEVELOPMENT,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = mutableListOf(expectedEntityStep),
      // JPA managed fields - expect these all to be null, implying a new db entity
      id = null,
      createdAt = null,
      createdBy = null,
      updatedAt = null,
      updatedBy = null,
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
    val createdAt = Instant.now()
    val updatedAt = Instant.now()
    val reviewDate = LocalDate.now().plusMonths(6)

    val entityStep = aValidStepEntity()
    val entityGoal = aValidGoalEntity(
      id = UUID.randomUUID(),
      reference = UUID.randomUUID(),
      title = "Improve communication skills",
      reviewDate = reviewDate,
      category = EntityCategory.PERSONAL_DEVELOPMENT,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = mutableListOf(entityStep),
      createdAt = createdAt,
      createdBy = "a.user.id",
      updatedAt = updatedAt,
      updatedBy = "another.user.id",
    )

    val domainStep = aValidStep()
    given(stepMapper.fromEntityToDomain(any())).willReturn(domainStep)

    val expected = aValidGoal(
      reference = entityGoal.reference!!,
      title = "Improve communication skills",
      reviewDate = reviewDate,
      category = DomainCategory.PERSONAL_DEVELOPMENT,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(domainStep),
      createdAt = createdAt,
      createdBy = "a.user.id",
      lastUpdatedAt = updatedAt,
      lastUpdatedBy = "another.user.id",
    )

    // When
    val actual = mapper.fromEntityToDomain(entityGoal)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromEntityToDomain(entityStep)
  }
}
