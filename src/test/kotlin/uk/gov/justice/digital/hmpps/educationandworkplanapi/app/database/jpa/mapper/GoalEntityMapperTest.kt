package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Spy
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

  @Spy
  private val stepMapper = StepEntityMapperImpl()

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

  @Test
  fun `should update entity from domain without updating JPA managed fields`() {
    // Given
    val id = UUID.randomUUID()
    val createdAt = Instant.now().minusSeconds(60)
    val createdBy = "a.user.id"
    val updatedAt = Instant.now()
    val updatedBy = "another.user.id"

    val existingEntity = aValidGoalEntity(
      id = id,
      createdAt = createdAt,
      createdBy = createdBy,
      updatedAt = updatedAt,
      updatedBy = updatedBy,
      title = "Improve communication skills",
      reviewDate = LocalDate.now().plusMonths(6),
    )

    val updateTitle = "Improve communication skills within 3 months"
    val updatedReviewDate = LocalDate.now().plusMonths(3)
    val domainGoal = aValidGoal(
      title = updateTitle,
      reviewDate = updatedReviewDate,
    )

    // When
    val actual = mapper.updateEntityFromDomain(existingEntity, domainGoal)

    // Then
    assertThat(actual)
      .hasId(id)
      .wasCreatedBy(createdBy)
      .wasCreatedAt(createdAt)
      .wasUpdatedBy(updatedBy)
      .wasUpdatedAt(updatedAt)
      .hasTitle(updateTitle)
      .hasReviewDate(updatedReviewDate)
  }

  @Test
  fun `should update entity from domain given removed step`() {
    // Given
    val id = UUID.randomUUID()
    val createdAt = Instant.now().minusSeconds(60)
    val createdBy = "a.user.id"
    val updatedAt = Instant.now()
    val updatedBy = "another.user.id"

    val step1Reference = UUID.randomUUID()
    val step1TargetDate = LocalDate.now().plusMonths(6)
    val step2Reference = UUID.randomUUID()
    val existingEntity = aValidGoalEntity(
      steps = mutableListOf(
        aValidStepEntity(
          id = id,
          createdAt = createdAt,
          createdBy = createdBy,
          updatedAt = updatedAt,
          updatedBy = updatedBy,
          reference = step1Reference,
          title = "Book communication skills course",
          sequenceNumber = 1,
          targetDate = step1TargetDate,
        ),
        aValidStepEntity(
          reference = step2Reference,
          title = "Attend communication skills course",
          sequenceNumber = 2,
        ),
      ),
    )

    val domainGoal = aValidGoal(
      steps = listOf(
        aValidStep(
          reference = step1Reference,
          title = "Book communication skills course",
          targetDate = step1TargetDate,
          sequenceNumber = 1,
        ),
      ),
    )

    // When
    val actual = mapper.updateEntityFromDomain(existingEntity, domainGoal)

    // Then
    assertThat(actual)
      .hasNumberOfSteps(1)
    assertThat(actual.steps!![0])
      .hasId(id)
      .wasCreatedBy(createdBy)
      .wasCreatedAt(createdAt)
      .wasUpdatedBy(updatedBy)
      .wasUpdatedAt(updatedAt)
      .hasReference(step1Reference)
      .hasTitle("Book communication skills course")
      .hasTargetDate(step1TargetDate)
  }

  @Test
  fun `should update entity from domain given added step`() {
    // Given
    val id = UUID.randomUUID()
    val createdAt = Instant.now().minusSeconds(60)
    val createdBy = "a.user.id"
    val updatedAt = Instant.now()
    val updatedBy = "another.user.id"

    val step1Reference = UUID.randomUUID()
    val step1TargetDate = LocalDate.now().plusMonths(6)
    val existingEntity = aValidGoalEntity(
      steps = mutableListOf(
        aValidStepEntity(
          id = id,
          createdAt = createdAt,
          createdBy = createdBy,
          updatedAt = updatedAt,
          updatedBy = updatedBy,
          reference = step1Reference,
          title = "Book communication skills course",
          sequenceNumber = 1,
          targetDate = step1TargetDate,
        ),
      ),
    )

    val step2Reference = UUID.randomUUID()
    val step2TargetDate = LocalDate.now().plusMonths(12)
    val domainGoal = aValidGoal(
      steps = listOf(
        aValidStep(
          reference = step1Reference,
          title = "Book communication skills course",
          targetDate = step1TargetDate,
          sequenceNumber = 1,
        ),
        aValidStep(
          reference = step2Reference,
          title = "Attend communication skills course",
          targetDate = step2TargetDate,
          sequenceNumber = 2,
        ),
      ),
    )

    // When
    val actual = mapper.updateEntityFromDomain(existingEntity, domainGoal)

    // Then
    assertThat(actual)
      .hasNumberOfSteps(2)
    assertThat(actual.steps!!.find { it.reference == step1Reference })
      .hasId(id)
      .wasCreatedBy(createdBy)
      .wasCreatedAt(createdAt)
      .wasUpdatedBy(updatedBy)
      .wasUpdatedAt(updatedAt)
      .hasTitle("Book communication skills course")
      .hasTargetDate(step1TargetDate)
    assertThat(actual.steps!!.find { it.reference == step2Reference })
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasTitle("Attend communication skills course")
      .hasTargetDate(step2TargetDate)
  }

  @Test
  fun `should update entity from domain given updated step`() {
    // Given
    val id = UUID.randomUUID()
    val createdAt = Instant.now().minusSeconds(60)
    val createdBy = "a.user.id"
    val updatedAt = Instant.now()
    val updatedBy = "another.user.id"

    val stepReference = UUID.randomUUID()
    val existingEntity = aValidGoalEntity(
      steps = mutableListOf(
        aValidStepEntity(
          id = id,
          createdAt = createdAt,
          createdBy = createdBy,
          updatedAt = updatedAt,
          updatedBy = updatedBy,
          reference = stepReference,
          title = "Book communication skills course",
          sequenceNumber = 1,
          targetDate = LocalDate.now().plusMonths(12),
        ),
      ),
    )

    val updateTitle = "Book communication skills course with education provider"
    val updatedTargetDate = LocalDate.now().plusMonths(3)
    val domainGoal = aValidGoal(
      steps = listOf(
        aValidStep(
          reference = stepReference,
          title = updateTitle,
          targetDate = updatedTargetDate,
          sequenceNumber = 1,
        ),
      ),
    )

    // When
    val actual = mapper.updateEntityFromDomain(existingEntity, domainGoal)

    // Then
    assertThat(actual)
      .hasNumberOfSteps(1)
    assertThat(actual.steps!![0])
      .hasId(id)
      .wasCreatedBy(createdBy)
      .wasCreatedAt(createdAt)
      .wasUpdatedBy(updatedBy)
      .wasUpdatedAt(updatedAt)
      .hasTitle(updateTitle)
      .hasTargetDate(updatedTargetDate)
  }
}
