package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidReference
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.GoalStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidGoalEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidGoal
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import java.time.LocalDate

/**
 * Unit test class for [GoalEntityMapper] specifically for the [GoalEntityMapper.updateEntityFromDomain] method.
 *
 * The tests in this class test the [GoalEntityMapper] but with a concrete [StepEntityMapper] rather than a mock.
 * This is because the method under test ([GoalEntityMapper.updateEntityFromDomain]) mutates the steps collection using
 * the [StepEntityMapper].
 * It is set with reflection as there is no setter or constructor injection on the mapstruct generated class.
 */
class GoalEntityMapperUpdateEntityFromDomainTest {

  private val mapper: GoalEntityMapper = GoalEntityMapperImpl().also {
    GoalEntityMapper::class.java.getDeclaredField("stepEntityMapper").apply {
      isAccessible = true
      set(it, StepEntityMapperImpl())
    }
  }

  @Test
  fun `should update entity from domain given domain goal has updates to goal fields`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStepEntity(
          reference = stepReference,
          title = "Book communication skills course",
          targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
          status = StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )

    val domainGoal = aValidGoal(
      reference = goalReference,
      title = "Improve communication skills within first 3 months",
      reviewDate = LocalDate.now().plusMonths(3),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.COMPLETED,
      notes = "Chris would like to improve his listening skills, not just his verbal communication; so that he can integrate with prison life",
      steps = listOf(
        aValidStep(
          reference = stepReference,
          title = "Book communication skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy().apply {
      title = "Improve communication skills within first 3 months"
      reviewDate = LocalDate.now().plusMonths(3)
      status = GoalStatus.COMPLETED
      notes = "Chris would like to improve his listening skills, not just his verbal communication; so that he can integrate with prison life"
    }

    // When
    mapper.updateEntityFromDomain(goalEntity, domainGoal)

    // Then
    assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
  }

  @Test
  fun `should update entity from domain given domain step has changes to step fields`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val stepEntity = aValidStepEntity(
      reference = stepReference,
      title = "Book communication skills course",
      targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(stepEntity),
    )

    val domainGoal = aValidGoal(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStep(
          reference = stepReference,
          title = "Book communication skills course within 6 months",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.THREE_TO_SIX_MONTHS,
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
          targetDateRange = TargetDateRange.THREE_TO_SIX_MONTHS
          status = StepStatus.COMPLETE
          sequenceNumber = 1
        },
      )
    }

    // When
    mapper.updateEntityFromDomain(goalEntity, domainGoal)

    // Then
    assertThat(goalEntity).isEqualToComparingAllFields(expectedGoalEntity)
  }

  @Test
  fun `should update entity from domain given new step added`() {
    // Given
    val goalReference = aValidReference()
    val step1Reference = aValidReference()
    val step2Reference = aValidReference()

    val step1Entity = aValidStepEntity(
      reference = step1Reference,
      title = "Book communication skills course",
      targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(step1Entity),
    )

    val domainGoal = aValidGoal(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStep(
          reference = step1Reference,
          title = "Book communication skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidStep(
          reference = step2Reference,
          title = "Attend skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.SIX_TO_TWELVE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
      ),
    )

    val expectedStep2Entity = aValidStepEntity(
      reference = step2Reference,
      title = "Attend skills course",
      targetDateRange = TargetDateRange.SIX_TO_TWELVE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )
    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDomain(goalEntity, domainGoal)

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
  fun `should update entity from domain given steps re-ordered and new step added in the middle`() {
    // Given
    val goalReference = aValidReference()
    val step1Reference = aValidReference()
    val step2Reference = aValidReference()

    val step1Entity = aValidStepEntity(
      reference = step1Reference,
      title = "Book communication skills course",
      targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val step2Entity = aValidStepEntity(
      reference = step2Reference,
      title = "Attend skills course",
      targetDateRange = TargetDateRange.SIX_TO_TWELVE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(step1Entity, step2Entity),
    )

    val newStepReference = aValidReference()

    val domainGoal = aValidGoal(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStep(
          reference = step1Reference,
          title = "Book communication skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidStep(
          reference = newStepReference,
          title = "Do pre-course homework and preparation",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
        aValidStep(
          reference = step2Reference,
          title = "Attend skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.SIX_TO_TWELVE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 3,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDomain(goalEntity, domainGoal)

    // Then
    assertThat(goalEntity)
      .hasNumberOfSteps(3)
      .isEqualToIgnoringSteps(expectedGoalEntity)
    assertThat(goalEntity.steps!![0]).isEqualToComparingAllFields(step1Entity)
    assertThat(goalEntity.steps!![1])
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasReference(newStepReference)
      .hasTitle("Do pre-course homework and preparation")
      .hasTargetDateRange(TargetDateRange.ZERO_TO_THREE_MONTHS)
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(2)
    assertThat(goalEntity.steps!![2])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step2Reference)
      .hasTitle("Attend skills course")
      .hasTargetDateRange(TargetDateRange.SIX_TO_TWELVE_MONTHS)
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(3)
  }

  @Test
  fun `should update entity from domain given steps re-ordered`() {
    // Given
    val goalReference = aValidReference()
    val step1Reference = aValidReference()
    val step2Reference = aValidReference()

    val step1Entity = aValidStepEntity(
      reference = step1Reference,
      title = "Book communication skills course",
      targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val step2Entity = aValidStepEntity(
      reference = step2Reference,
      title = "Attend skills course",
      targetDateRange = TargetDateRange.SIX_TO_TWELVE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(step1Entity, step2Entity),
    )

    val domainGoal = aValidGoal(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStep(
          reference = step2Reference,
          title = "Attend skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.SIX_TO_TWELVE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
        aValidStep(
          reference = step1Reference,
          title = "Book communication skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDomain(goalEntity, domainGoal)

    // Then
    assertThat(goalEntity)
      .hasNumberOfSteps(2)
      .isEqualToIgnoringSteps(expectedGoalEntity)
    assertThat(goalEntity.steps!![0])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step2Reference)
      .hasTitle("Attend skills course")
      .hasTargetDateRange(TargetDateRange.SIX_TO_TWELVE_MONTHS)
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(1)
    assertThat(goalEntity.steps!![1])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step1Reference)
      .hasTitle("Book communication skills course")
      .hasTargetDateRange(TargetDateRange.ZERO_TO_THREE_MONTHS)
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(2)
  }

  @Test
  fun `should update entity from domain given steps removed`() {
    // Given
    val goalReference = aValidReference()
    val step1Reference = aValidReference()
    val step2Reference = aValidReference()

    val step1Entity = aValidStepEntity(
      reference = step1Reference,
      title = "Book communication skills course",
      targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 1,
    )
    val step2Entity = aValidStepEntity(
      reference = step2Reference,
      title = "Attend skills course",
      targetDateRange = TargetDateRange.SIX_TO_TWELVE_MONTHS,
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )
    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(step1Entity, step2Entity),
    )

    val domainGoal = aValidGoal(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStep(
          reference = step2Reference,
          title = "Attend skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.SIX_TO_TWELVE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 2,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDomain(goalEntity, domainGoal)

    // Then
    assertThat(goalEntity)
      .hasNumberOfSteps(1)
      .isEqualToIgnoringSteps(expectedGoalEntity)
    assertThat(goalEntity.steps!![0])
      .hasJpaManagedFieldsPopulated()
      .hasReference(step2Reference)
      .hasTitle("Attend skills course")
      .hasTargetDateRange(TargetDateRange.SIX_TO_TWELVE_MONTHS)
      .hasStatus(StepStatus.ACTIVE)
      .hasSequenceNumber(1)
  }

  @Test
  fun `should update entity from domain given domain goal has no changes to entity goal`() {
    // Given
    val goalReference = aValidReference()
    val stepReference = aValidReference()

    val goalEntity = aValidGoalEntity(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStepEntity(
          reference = stepReference,
          title = "Book communication skills course",
          targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
          status = StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )

    val domainGoal = aValidGoal(
      reference = goalReference,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
      status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.GoalStatus.ACTIVE,
      notes = "Chris would like to improve his listening skills, not just his verbal communication",
      steps = listOf(
        aValidStep(
          reference = stepReference,
          title = "Book communication skills course",
          targetDateRange = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange.ZERO_TO_THREE_MONTHS,
          status = uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus.ACTIVE,
          sequenceNumber = 1,
        ),
      ),
    )

    val expectedGoalEntity = goalEntity.deepCopy()

    // When
    mapper.updateEntityFromDomain(goalEntity, domainGoal)

    // Then
    assertThat(goalEntity).usingRecursiveComparison().isEqualTo(expectedGoalEntity)
  }
}