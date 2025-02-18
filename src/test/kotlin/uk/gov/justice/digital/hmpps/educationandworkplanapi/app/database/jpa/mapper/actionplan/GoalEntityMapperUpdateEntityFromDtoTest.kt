package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateGoalDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import java.time.LocalDate

/**
 * Unit test class for [GoalEntityMapper] specifically for the [GoalEntityMapper.updateEntityFromDto] method.
 *
 * The tests in this class test the [GoalEntityMapper] but with concrete [GoalEntityListManager] and [StepEntityMapper]
 * instances rather than mocks.
 * This is because the method under test ([GoalEntityMapper.updateEntityFromDto]) mutates the steps collection using
 * the [GoalEntityListManager] and [StepEntityMapper].
 */
class GoalEntityMapperUpdateEntityFromDtoTest {

  private val mapper: GoalEntityMapper = GoalEntityMapper(StepEntityMapper(), GoalEntityListManager())

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
      steps = listOf(
        aValidUpdateStepDto(
          reference = stepReference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )

    // When
    mapper.updateEntityFromDto(goalEntity, updateGoalDto)

    // Then
    assertThat(goalEntity)
      .hasTitle("Improve communication skills within first 3 months")
      .hasTargetCompletionDate(LocalDate.now().plusMonths(3))
      .hasStatus(GoalStatus.ACTIVE)
      .wasUpdatedAtPrison("MDI")
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
      steps = listOf(stepEntity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      steps = listOf(
        aValidUpdateStepDto(
          reference = stepReference,
          title = "Book communication skills course within 6 months",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.COMPLETE,
          sequenceNumber = 1,
        ),
      ),
    )

    // When
    mapper.updateEntityFromDto(goalEntity, updateGoalDto)

    // Then
    assertThat(goalEntity)
      .hasTitle("Improve communication skills")
      .hasTargetCompletionDate(LocalDate.now().plusMonths(6))
      .hasNumberOfSteps(1)
      .stepWithSequenceNumber(1) {
        it.hasTitle("Book communication skills course within 6 months")
          .hasStatus(StepStatus.COMPLETE)
      }
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
      steps = listOf(step1Entity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      prisonId = "BXI",
      steps = listOf(
        aValidUpdateStepDto(
          reference = step1Reference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidUpdateStepDto(
          reference = step2Reference,
          title = "Attend skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
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
      .isEqualToIgnoringStepsAndNotes(expectedGoalEntity)
    assertThat(goalEntity.steps[0]).isEqualToComparingAllFields(step1Entity)
    assertThat(goalEntity.steps[1])
      .doesNotHaveJpaManagedFieldsPopulated()
      .isEqualToIgnoringJpaManagedFields(expectedStep2Entity)
  }

  @Test
  fun `should update entity from UpdateGoalDto given DTO has steps re-ordered and new step added in the middle`() {
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
      steps = listOf(
        aValidUpdateStepDto(
          reference = step1Reference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidUpdateStepDto(
          reference = newStepReference,
          title = "Do pre-course homework and preparation",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
        aValidUpdateStepDto(
          reference = step2Reference,
          title = "Attend skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
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
      .isEqualToIgnoringStepsAndNotes(expectedGoalEntity)
    assertThat(goalEntity.steps[0]).isEqualToComparingAllFields(step1Entity)
    assertThat(goalEntity.steps[1])
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasReference(newStepReference)
      .hasTitle("Do pre-course homework and preparation")
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(2)
    assertThat(goalEntity.steps[2])
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
      steps = listOf(step1Entity, step2Entity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      steps = listOf(
        aValidUpdateStepDto(
          reference = step2Reference,
          title = "Attend skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidUpdateStepDto(
          reference = step1Reference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
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
      .isEqualToIgnoringStepsAndNotes(expectedGoalEntity)
    assertThat(goalEntity.steps[0])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step2Reference)
      .hasTitle("Attend skills course")
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(1)
    assertThat(goalEntity.steps[1])
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
      steps = listOf(step1Entity, step2Entity),
      createdAtPrison = "BXI",
      updatedAtPrison = "BXI",
    )

    val updateGoalDto = aValidUpdateGoalDto(
      reference = goalReference,
      title = "Improve communication skills",
      prisonId = "BXI",
      targetCompletionDate = LocalDate.now().plusMonths(6),
      steps = listOf(
        aValidUpdateStepDto(
          reference = step2Reference,
          title = "Attend skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
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
      .isEqualToIgnoringStepsAndNotes(expectedGoalEntity)
    assertThat(goalEntity.steps[0])
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
      steps = listOf(
        aValidUpdateStepDto(
          reference = stepReference,
          title = "Book communication skills course",
          status = uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDto(goalEntity, domainGoal)

    // Then
    assertThat(goalEntity).isEqualToComparingAllFieldsExceptNotes(expectedGoalEntity)
  }
}
