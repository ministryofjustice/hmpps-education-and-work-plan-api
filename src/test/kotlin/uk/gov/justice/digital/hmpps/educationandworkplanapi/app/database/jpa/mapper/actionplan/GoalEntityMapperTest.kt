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
import uk.gov.justice.digital.hmpps.domain.personallearningplan.Step
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidStep
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.CreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus as DomainStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus as EntityStatus

@ExtendWith(MockitoExtension::class)
class GoalEntityMapperTest {

  @InjectMocks
  private lateinit var mapper: GoalEntityMapper

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
      steps = listOf(createStepDto),
    )

    val expectedEntityStep = aValidStepEntity()
    given(stepMapper.fromDtoToEntity(any<CreateStepDto>())).willReturn(expectedEntityStep)

    val expected = aValidGoalEntity(
      title = "Improve communication skills",
      targetCompletionDate = targetCompletionDate,
      status = EntityStatus.ACTIVE,
      steps = mutableListOf(expectedEntityStep),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
      // JPA managed fields - expect these all to be null, implying a new db entity
      id = null,
      createdAt = null,
      createdBy = null,
      updatedAt = null,
      updatedBy = null,
    )

    // When
    val actual = mapper.fromDtoToEntity(createGoalDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .usingRecursiveComparison()
      .ignoringFields("reference", "notes")
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
      steps = mutableListOf(entityStep),
      createdAtPrison = "BXI",
      updatedAtPrison = "MDI",
      createdAt = createdAt,
      createdBy = "asmith_gen",
      updatedAt = updatedAt,
      updatedBy = "bjones_gen",
    )

    val domainStep = aValidStep()
    given(stepMapper.fromEntityToDomain(any())).willReturn(domainStep)

    val expected = aValidGoal(
      reference = entityGoal.reference,
      title = "Improve communication skills",
      targetCompletionDate = targetCompletionDate,
      status = DomainStatus.ACTIVE,
      steps = listOf(domainStep),
      createdAt = createdAt,
      createdAtPrison = "BXI",
      createdBy = "asmith_gen",
      lastUpdatedAt = updatedAt,
      lastUpdatedAtPrison = "MDI",
      lastUpdatedBy = "bjones_gen",
    )

    // When
    val actual = mapper.fromEntityToDomain(entityGoal)

    // Then
    assertThat(actual).usingRecursiveComparison().ignoringFields("notes").isEqualTo(expected)
    verify(stepMapper).fromEntityToDomain(entityStep)
  }
}
