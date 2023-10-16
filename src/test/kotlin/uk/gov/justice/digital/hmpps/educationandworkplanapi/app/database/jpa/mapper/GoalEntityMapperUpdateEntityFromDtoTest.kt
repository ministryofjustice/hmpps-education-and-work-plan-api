package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidUpdateGoalDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidUpdateStepDto
import java.time.LocalDate

/**
 * Unit test class for [GoalEntityMapper] specifically for the [GoalEntityMapper.updateEntityFromDto] method.
 *
 * The tests in this class test the [GoalEntityMapper] but with a concrete [StepEntityMapper] rather than a mock.
 * This is because the method under test ([GoalEntityMapper.updateEntityFromDto]) mutates the steps collection using
 * the [StepEntityMapper].
 * It is set with reflection as there is no setter or constructor injection on the mapstruct generated class.
 */
class GoalEntityMapperUpdateEntityFromDtoTest {

  private val mapper: GoalEntityMapper = GoalEntityMapperImpl().also {
    GoalEntityMapper::class.java.getDeclaredField("stepEntityMapper").apply {
      isAccessible = true
      set(it, StepEntityMapperImpl())
    }
  }

  @Test
  fun `should update entity from UpdateGoalDto given DTO has updates to goal fields`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStepEntity(
          reference = stepReference,
          title = "Book communication skills course",
          status = StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills within first 3 months",
      prisonId = "MDI",
      targetCompletionDate = LocalDate.now().plusMonths(3),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.COMPLETED,
      notes = "Chris would like to improve his listening skills, not just his verbal communication; so that he can integrate with prison life",
      steps = listOf(
        aValidUpdateStepDto(
          reference = stepReference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy().apply {
      title = "Improve communication skills within first 3 months"
      targetCompletionDate = LocalDate.now().plusMonths(3)
      status = GoalStatus.COMPLETED
      notes = "Chris would like to improve his listening skills, not just his verbal communication; so that he can integrate with prison life"
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateEntityFromDto(goalEntity, updateGoalDto)

    // Then
    assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
  }

  @Test
  fun `should update entity from UpdateGoalDto given DTO has changes to step fields`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val stepEntity = aValidStepEntity(
      reference = stepReference,
      title = "Book communication skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(stepEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidUpdateStepDto(
          reference = stepReference,
          title = "Book communication skills course within 6 months",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.COMPLETE,
          sequenceNumber = 1,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy().apply {
      steps = mutableListOf(
        stepEntity.deepCopy().apply {
          reference = stepReference
          title = "Book communication skills course within 6 months"
          status = StepStatus.COMPLETE
          sequenceNumber = 1
        },
      )
    }

    // When
    mapper.updateEntityFromDto(goalEntity, updateGoalDto)

    // Then
    assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
  }

  @Test
  fun `should update entity from UpdateGoalDto given DTO has new step added`() {
    // Given
    val goalReference = aValidReference()
    val step1Reference = aValidReference()
    val step2Reference = aValidReference()

    val step1Entity = aValidStepEntity(
      reference = step1Reference,
      title = "Book communication skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(step1Entity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      prisonId = "BXI",
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidUpdateStepDto(
          reference = step1Reference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidUpdateStepDto(
          reference = step2Reference,
          title = "Attend skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
      ),
    )

    val expectedStep2Entity = aValidStepEntity(
      reference = step2Reference,
      title = "Attend skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )
    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDto(goalEntity, updateGoalDto)

    // Then
    assertThat(goalEntity)
      .hasNumberOfSteps(2)
      .isEqualToIgnoringSteps(expectedGoalEntity)
    assertThat(goalEntity.steps!![0]).isEqualToComparingAllFields(step1Entity)
    assertThat(goalEntity.steps!![1])
      .doesNotHaveJpaManagedFieldsPopulated()
      .isEqualToIgnoringJpaManagedFields(expectedStep2Entity)
  }

  @Test
  fun `should update entity from UpdateGoalDto given DTO given steps re-ordered and new step added in the middle`() {
    // Given
    val goalReference = aValidReference()
    val step1Reference = aValidReference()
    val step2Reference = aValidReference()

    val step1Entity = aValidStepEntity(
      reference = step1Reference,
      title = "Book communication skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val step2Entity = aValidStepEntity(
      reference = step2Reference,
      title = "Attend skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(step1Entity, step2Entity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val newStepReference = aValidReference()

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidUpdateStepDto(
          reference = step1Reference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidUpdateStepDto(
          reference = newStepReference,
          title = "Do pre-course homework and preparation",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
        aValidUpdateStepDto(
          reference = step2Reference,
          title = "Attend skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 3,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDto(goalEntity, updateGoalDto)

    // Then
    assertThat(goalEntity)
      .hasNumberOfSteps(3)
      .isEqualToIgnoringSteps(expectedGoalEntity)
    assertThat(goalEntity.steps!![0]).isEqualToComparingAllFields(step1Entity)
    assertThat(goalEntity.steps!![1])
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasReference(newStepReference)
      .hasTitle("Do pre-course homework and preparation")
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(2)
    assertThat(goalEntity.steps!![2])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step2Reference)
      .hasTitle("Attend skills course")
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(3)
  }

  @Test
  fun `should update entity from UpdateGoalDto given DTO given steps re-ordered`() {
    // Given
    val goalReference = aValidReference()
    val step1Reference = aValidReference()
    val step2Reference = aValidReference()

    val step1Entity = aValidStepEntity(
      reference = step1Reference,
      title = "Book communication skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val step2Entity = aValidStepEntity(
      reference = step2Reference,
      title = "Attend skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(step1Entity, step2Entity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidUpdateStepDto(
          reference = step2Reference,
          title = "Attend skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidUpdateStepDto(
          reference = step1Reference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDto(goalEntity, updateGoalDto)

    // Then
    assertThat(goalEntity)
      .hasNumberOfSteps(2)
      .isEqualToIgnoringSteps(expectedGoalEntity)
    assertThat(goalEntity.steps!![0])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step2Reference)
      .hasTitle("Attend skills course")
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(1)
    assertThat(goalEntity.steps!![1])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step1Reference)
      .hasTitle("Book communication skills course")
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(2)
  }

  @Test
  fun `should update entity from UpdateGoalDto given DTO has steps removed`() {
    // Given
    val goalReference = aValidReference()
    val step1Reference = aValidReference()
    val step2Reference = aValidReference()

    val step1Entity = aValidStepEntity(
      reference = step1Reference,
      title = "Book communication skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val step2Entity = aValidStepEntity(
      reference = step2Reference,
      title = "Attend skills course",
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(step1Entity, step2Entity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidUpdateStepDto(
          reference = step2Reference,
          title = "Attend skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDto(goalEntity, updateGoalDto)

    // Then
    assertThat(goalEntity)
      .hasNumberOfSteps(1)
      .isEqualToIgnoringSteps(expectedGoalEntity)
    assertThat(goalEntity.steps!![0])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step2Reference)
      .hasTitle("Attend skills course")
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(1)
  }

  @Test
  fun `should update entity from UpdateGoalDto given DTO has no changes to entity goal`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStepEntity(
          reference = stepReference,
          title = "Book communication skills course",
          status = StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val domainGoal = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidUpdateStepDto(
          reference = stepReference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDto(goalEntity, domainGoal)

    // Then
    assertThat(goalEntity).usingRecursiveComparison().isEqualTo(expectedGoalEntity)
  }
}
