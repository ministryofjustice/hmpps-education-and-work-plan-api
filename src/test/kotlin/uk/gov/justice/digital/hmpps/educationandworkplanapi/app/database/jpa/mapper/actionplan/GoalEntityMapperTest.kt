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
import uk.gov.justice.digital.hmpps.domain.goal.Step
import uk.gov.justice.digital.hmpps.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.domain.goal.dto.CreateStepDto
import uk.gov.justice.digital.hmpps.domain.goal.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.domain.goal.dto.aValidCreateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.goal.GoalStatus as DomainStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus as EntityStatus

@ExtendWith(MockitoExtension::class)
class GoalEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: GoalEntityMapperImpl

  @Mock
  private lateinit var stepMapper: StepEntityMapper

  @Mock
  private lateinit var entityListManager: GoalEntityListManager<StepEntity, Step>

  @Test
  fun `should map from CreateGoalDto to entity`() {
    // Given
    val targetCompletionDate = LocalDate.now().plusMonths(6)

    val createStepDto = aValidCreateStepDto()
    val createGoalDto = aValidCreateGoalDto(
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = targetCompletionDate,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(createStepDto),
    )

    val expectedEntityStep = aValidStepEntity()
    given(stepMapper.fromDtoToEntity(any<CreateStepDto>())).willReturn(expectedEntityStep)

    val expected = aValidGoalEntity(
      title = "Improve communication skills",
      targetCompletionDate = targetCompletionDate,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = mutableListOf(expectedEntityStep),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
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
  fun `should map from GoalEntity to domain`() {
    // Given
    val createdAt = Instant.now()
    val updatedAt = Instant.now()
    val targetCompletionDate = LocalDate.now().plusMonths(6)

    val entityStep = aValidStepEntity()
    val entityGoal = aValidGoalEntity(
      id = UUID.randomUUID(),
      reference = UUID.randomUUID(),
      title = "Improve communication skills",
      targetCompletionDate = targetCompletionDate,
      status = EntityStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = mutableListOf(entityStep),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
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
      targetCompletionDate = targetCompletionDate,
      status = DomainStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(domainStep),
      createdAt = createdAt,
      createdAtPrison = "BXI",
      createdBy = "asmith_gen",
      createdByDisplayName = "Alex Smith",
      lastUpdatedAt = updatedAt,
      lastUpdatedAtPrison = "MDI",
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
