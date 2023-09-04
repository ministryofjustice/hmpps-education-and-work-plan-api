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
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateStepDto
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalStatus as EntityStatus
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

    val createStepDto = aValidCreateStepDto()
    val createGoalDto = aValidCreateGoalDto(
      title = "Improve communication skills",
      reviewDate = reviewDate,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(createStepDto),
    )

    val expectedEntityStep = aValidStepEntity()
    given(stepMapper.fromDtoToEntity(any())).willReturn(expectedEntityStep)

    val expected = aValidGoalEntity(
      title = "Improve communication skills",
      reviewDate = reviewDate,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = mutableListOf(expectedEntityStep),
      // JPA managed fields - expect these all to be null, implying a new db entity
      id = null,
      createdAt = null,
      createdBy = null,
      createdByDisplayName = null,
      updatedAt = null,
      updatedBy = null,
      updatedByDisplayName = null,
    )

    // When
    val actual = mapper.fromDtoToEntity(createGoalDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFields("reference")
      .isEqualTo(expected)
    verify(stepMapper).fromDtoToEntity(createStepDto)
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
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = mutableListOf(entityStep),
      createdAt = createdAt,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      updatedAt = updatedAt,
      updatedBy = "bjones_gen",
      updatedByDisplayName = "Barry Jones",
    )

    val domainStep = aValidStep()
    given(stepMapper.fromEntityToDomain(any())).willReturn(domainStep)

    val expected = aValidGoal(
      reference = entityGoal.reference!!,
      title = "Improve communication skills",
      reviewDate = reviewDate,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(domainStep),
      createdAt = createdAt,
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      lastUpdatedAt = updatedAt,
      lastUpdatedBy = "bjones_gen",
      lastUpdatedByDisplayName = "Barry Jones",
    )

    // When
    val actual = mapper.fromEntityToDomain(entityGoal)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    verify(stepMapper).fromEntityToDomain(entityStep)
  }
}
