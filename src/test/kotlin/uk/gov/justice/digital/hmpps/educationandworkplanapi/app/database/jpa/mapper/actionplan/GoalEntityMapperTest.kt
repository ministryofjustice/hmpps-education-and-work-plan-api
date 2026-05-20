package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.COMPLETE
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidGoal
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidStep
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat
import java.time.Instant
import java.time.LocalDate
import java.util.*
import uk.gov.justice.digital.hmpps.domain.personallearningplan.GoalStatus as DomainStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus as EntityStatus

class GoalEntityMapperTest {

  private val mapper = GoalEntityMapper(StepEntityMapper())

  @Nested
  inner class FromDtoToEntity {
    @Test
    fun `should map from CreateGoalDto to entity`() {
      // Given
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val createGoalDto = aValidCreateGoalDto(
        title = "Improve communication skills",
        prisonId = "BXI",
        targetCompletionDate = targetCompletionDate,
        status = DomainStatus.ACTIVE,
        notes = "Chris would like to improve his listening skills, not just his verbal communication",
        steps = listOf(
          aValidCreateStepDto(
            title = "Book communication skills course",
            status = NOT_STARTED,
            sequenceNumber = 1,
          ),
        ),
      )

      val expected = aValidGoalEntity(
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = EntityStatus.ACTIVE,
        notes = "Chris would like to improve his listening skills, not just his verbal communication",
        steps = mutableListOf(
          aValidStepEntity(
            title = "Book communication skills course",
            status = StepStatus.NOT_STARTED,
            sequenceNumber = 1,
            // JPA managed fields - expect these all to be null, implying a new db entity
            id = null,
            createdAt = null,
            createdBy = null,
            updatedAt = null,
            updatedBy = null,
          ),
        ),
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
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*reference") // Ignore the generated reference field as we cannot predict its value in the expected object
        .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between GoalEntity and StepEntity and is not mapped from the source DTO
        .isEqualTo(expected)
    }
  }

  @Nested
  inner class FromEntityToDomain {
    @Test
    fun `should map from GoalEntity to domain`() {
      // Given
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val entityStep = aValidStepEntity(
        id = UUID.randomUUID(),
        reference = UUID.randomUUID(),
        title = "Book communication skills course",
        status = StepStatus.NOT_STARTED,
        sequenceNumber = 1,
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )
      val entityGoal = aValidGoalEntity(
        id = UUID.randomUUID(),
        reference = UUID.randomUUID(),
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = EntityStatus.ACTIVE,
        steps = mutableListOf(entityStep),
        notes = "Chris would like to improve his listening skills, not just his verbal communication",
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val expected = aValidGoal(
        reference = entityGoal.reference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = DomainStatus.ACTIVE,
        steps = listOf(
          aValidStep(
            reference = entityStep.reference,
            title = "Book communication skills course",
            status = NOT_STARTED,
            sequenceNumber = 1,
          ),
        ),
        notes = "Chris would like to improve his listening skills, not just his verbal communication",
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
      assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    }
  }

  @Nested
  inner class UpdateEntityFromDto {
    @Test
    fun `should update entity from UpdateGoalDto given DTO only has updates to goal fields`() {
      // Given
      val goalId = aValidReference()
      val goalReference = aValidReference()
      val stepId = aValidReference()
      val stepReference = aValidReference()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val goalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        notes = "Chris would like to improve his listening skills",
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = stepReference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = stepId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val updateGoalDto = aValidUpdateGoalDto(
        reference = goalReference,
        title = "Improve communication skills within first 3 months",
        prisonId = "MDI",
        targetCompletionDate = LocalDate.now().plusMonths(3),
        notes = "Chris would like to improve his listening skills as well as his verbal skills",
        steps = listOf(
          aValidUpdateStepDto(
            reference = stepReference,
            title = "Book communication skills course",
            status = ACTIVE,
            sequenceNumber = 1,
          ),
        ),
      )

      val expectedGoalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills within first 3 months",
        targetCompletionDate = LocalDate.now().plusMonths(3),
        status = GoalStatus.ACTIVE,
        notes = "Chris would like to improve his listening skills as well as his verbal skills",
        steps = listOf(
          aValidStepEntity(
            reference = stepReference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = stepId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateEntityFromDto(goalEntity, updateGoalDto)

      // Then
      assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
    }

    @Test
    fun `should update entity from UpdateGoalDto given DTO has changes to step fields`() {
      // Given
      val goalId = aValidReference()
      val goalReference = aValidReference()
      val stepId = aValidReference()
      val stepReference = aValidReference()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val goalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = stepReference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = stepId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val updateGoalDto = aValidUpdateGoalDto(
        reference = goalReference,
        title = "Improve communication skills",
        prisonId = "MDI",
        targetCompletionDate = targetCompletionDate,
        notes = null,
        steps = listOf(
          aValidUpdateStepDto(
            reference = stepReference,
            title = "Book communication skills course within 6 months",
            status = COMPLETE,
            sequenceNumber = 1,
          ),
        ),
      )

      val expectedGoalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = stepReference,
            title = "Book communication skills course within 6 months",
            status = StepStatus.COMPLETE,
            sequenceNumber = 1,
            id = stepId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateEntityFromDto(goalEntity, updateGoalDto)

      // Then
      assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
    }

    @Test
    fun `should update entity from UpdateGoalDto given DTO has new step added`() {
      // Given
      val goalId = aValidReference()
      val goalReference = aValidReference()
      val step1Id = aValidReference()
      val step1Reference = aValidReference()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val goalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = step1Reference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = step1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val newStepReference = aValidReference()
      val updateGoalDto = aValidUpdateGoalDto(
        reference = goalReference,
        title = "Improve communication skills",
        prisonId = "MDI",
        targetCompletionDate = LocalDate.now().plusMonths(6),
        notes = null,
        steps = listOf(
          aValidUpdateStepDto(
            reference = step1Reference,
            title = "Book communication skills course",
            status = ACTIVE,
            sequenceNumber = 1,
          ),
          aValidUpdateStepDto(
            reference = newStepReference,
            title = "Attend skills course",
            status = ACTIVE,
            sequenceNumber = 2,
          ),
        ),
      )

      val expectedGoalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        notes = null,
        steps = listOf(
          aValidStepEntity(
            reference = step1Reference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = step1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
          aValidStepEntity(
            reference = newStepReference,
            title = "Attend skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 2,
            // JPA managed fields - expect these all to be null, implying a new db entity
            id = null,
            createdAt = null,
            createdBy = null,
            updatedAt = null,
            updatedBy = null,
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateEntityFromDto(goalEntity, updateGoalDto)

      // Then
      assertThat(goalEntity)
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between GoalEntity and StepEntity and is not mapped from the source DTO
        .isEqualTo(expectedGoalEntity)
    }

    @Test
    fun `should update entity from UpdateGoalDto given DTO has steps re-ordered and new step added in the middle`() {
      // Given
      val goalId = aValidReference()
      val goalReference = aValidReference()
      val step1Id = aValidReference()
      val step1Reference = aValidReference()
      val step2Id = aValidReference()
      val step2Reference = aValidReference()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val goalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = step1Reference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = step1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
          aValidStepEntity(
            reference = step2Reference,
            title = "Attend skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 2,
            id = step2Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val newStepReference = aValidReference()
      val updateGoalDto = aValidUpdateGoalDto(
        reference = goalReference,
        title = "Improve communication skills",
        prisonId = "MDI",
        targetCompletionDate = LocalDate.now().plusMonths(6),
        notes = null,
        steps = listOf(
          aValidUpdateStepDto(
            reference = step1Reference,
            title = "Book communication skills course",
            status = ACTIVE,
            sequenceNumber = 1,
          ),
          aValidUpdateStepDto(
            reference = newStepReference,
            title = "Do pre-course homework and preparation",
            status = ACTIVE,
            sequenceNumber = 2,
          ),
          aValidUpdateStepDto(
            reference = step2Reference,
            title = "Attend skills course",
            status = ACTIVE,
            sequenceNumber = 3, // Previously this was step 2 but its sequence number has been incremented to allow for the new step to be inserted into the sequence
          ),
        ),
      )

      val expectedGoalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        notes = null,
        steps = listOf(
          aValidStepEntity(
            reference = step1Reference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = step1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
          aValidStepEntity(
            reference = newStepReference,
            title = "Do pre-course homework and preparation",
            status = StepStatus.ACTIVE,
            sequenceNumber = 2,
            // JPA managed fields - expect these all to be null, implying a new db entity
            id = null,
            createdAt = null,
            createdBy = null,
            updatedAt = null,
            updatedBy = null,
          ),
          aValidStepEntity(
            reference = step2Reference,
            title = "Attend skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 3,
            id = step2Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateEntityFromDto(goalEntity, updateGoalDto)

      // Then
      assertThat(goalEntity)
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*parent") // Ignore the parent field as this is set to establish the bidirectional relationship between GoalEntity and StepEntity and is not mapped from the source DTO
        .isEqualTo(expectedGoalEntity)
    }

    @Test
    fun `should update entity from UpdateGoalDto given DTO given steps re-ordered`() {
      // Given
      val goalId = aValidReference()
      val goalReference = aValidReference()
      val step1Id = aValidReference()
      val step1Reference = aValidReference()
      val step2Id = aValidReference()
      val step2Reference = aValidReference()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val goalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = step1Reference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = step1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
          aValidStepEntity(
            reference = step2Reference,
            title = "Attend skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 2,
            id = step2Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val updateGoalDto = aValidUpdateGoalDto(
        reference = goalReference,
        title = "Improve communication skills",
        prisonId = "MDI",
        targetCompletionDate = LocalDate.now().plusMonths(6),
        notes = null,
        steps = listOf(
          aValidUpdateStepDto(
            reference = step2Reference,
            title = "Attend skills course",
            status = ACTIVE,
            sequenceNumber = 1,
          ),
          aValidUpdateStepDto(
            reference = step1Reference,
            title = "Book communication skills course",
            status = ACTIVE,
            sequenceNumber = 2,
          ),
        ),
      )

      val expectedGoalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        notes = null,
        steps = listOf(
          aValidStepEntity(
            reference = step2Reference,
            title = "Attend skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = step2Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
          aValidStepEntity(
            reference = step1Reference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 2,
            id = step1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateEntityFromDto(goalEntity, updateGoalDto)

      // Then
      assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
    }

    @Test
    fun `should update entity from UpdateGoalDto given DTO has steps removed`() {
      // Given
      val goalId = aValidReference()
      val goalReference = aValidReference()
      val step1Id = aValidReference()
      val step1Reference = aValidReference()
      val step2Id = aValidReference()
      val step2Reference = aValidReference()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val goalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = step1Reference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = step1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
          aValidStepEntity(
            reference = step2Reference,
            title = "Attend skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 2,
            id = step2Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val step1Dto = aValidUpdateStepDto(
        reference = step1Reference,
        title = "Book communication skills course",
        status = ACTIVE,
        sequenceNumber = 1,
      )
      val updateGoalDto = aValidUpdateGoalDto(
        reference = goalReference,
        title = "Improve communication skills",
        prisonId = "MDI",
        targetCompletionDate = LocalDate.now().plusMonths(6),
        notes = null,
        steps = listOf(step1Dto), // The DTO only contains step 1, hence step 2 will be removed
      )

      val expectedGoalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = step1Reference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = step1Id,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateEntityFromDto(goalEntity, updateGoalDto)

      // Then
      assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
    }

    @Test
    fun `should update entity from UpdateGoalDto given DTO has no effective changes to goal or steps`() {
      // Given
      val goalId = aValidReference()
      val goalReference = aValidReference()
      val stepId = aValidReference()
      val stepReference = aValidReference()
      val createdAt = Instant.now()
      val updatedAt = Instant.now()
      val targetCompletionDate = LocalDate.now().plusMonths(6)

      val goalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = stepReference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = stepId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "BXI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      val stepDto = aValidUpdateStepDto(
        reference = stepReference,
        title = "Book communication skills course",
        status = ACTIVE,
        sequenceNumber = 1,
      )
      val updateGoalDto = aValidUpdateGoalDto(
        reference = goalReference,
        title = "Improve communication skills",
        prisonId = "MDI",
        targetCompletionDate = LocalDate.now().plusMonths(6),
        notes = null,
        steps = listOf(stepDto),
      )

      val expectedGoalEntity = aValidGoalEntity(
        reference = goalReference,
        title = "Improve communication skills",
        targetCompletionDate = targetCompletionDate,
        status = GoalStatus.ACTIVE,
        steps = listOf(
          aValidStepEntity(
            reference = stepReference,
            title = "Book communication skills course",
            status = StepStatus.ACTIVE,
            sequenceNumber = 1,
            id = stepId,
            createdAt = createdAt,
            createdBy = "asmith_gen",
            updatedAt = updatedAt,
            updatedBy = "bjones_gen",
          ),
        ),
        id = goalId,
        createdAtPrison = "BXI",
        updatedAtPrison = "MDI",
        createdAt = createdAt,
        createdBy = "asmith_gen",
        updatedAt = updatedAt,
        updatedBy = "bjones_gen",
      )

      // When
      mapper.updateEntityFromDto(goalEntity, updateGoalDto)

      // Then
      assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
    }
  }
}
