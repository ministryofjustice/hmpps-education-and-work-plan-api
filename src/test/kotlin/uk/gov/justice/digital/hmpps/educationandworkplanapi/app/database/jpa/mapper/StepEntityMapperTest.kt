package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus as EntityStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus as DomainStatus

class StepEntityMapperTest {

  private val mapper = StepEntityMapperImpl()

  @Test
  fun `should map from domain to entity`() {
    // Given
    val targetDate = LocalDate.now().plusMonths(6)

    val domainStep = aValidStep(
      reference = UUID.randomUUID(),
      title = "Book communication skills course",
      targetDate = targetDate,
      status = DomainStatus.ACTIVE,
      sequenceNumber = 1,
    )

    val expected = aValidStepEntity(
      reference = domainStep.reference,
      title = "Book communication skills course",
      targetDate = targetDate,
      status = EntityStatus.ACTIVE,
      sequenceNumber = 1,
      // JPA managed fields - expect these all to be null, implying a new db entity
      id = null,
      createdAt = null,
      createdBy = null,
      updatedAt = null,
      updatedBy = null,
    )

    // When
    val actual = mapper.fromDomainToEntity(domainStep)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val entityStep = aValidStepEntity(
      id = null,
      reference = UUID.randomUUID(),
      title = "Book communication skills course",
      targetDate = LocalDate.now().plusMonths(6),
      status = EntityStatus.ACTIVE,
      sequenceNumber = 1,
    )

    val expected = aValidStep(
      reference = entityStep.reference!!,
      title = "Book communication skills course",
      targetDate = LocalDate.now().plusMonths(6),
      status = DomainStatus.ACTIVE,
      sequenceNumber = 1,
    )

    // When
    val actual = mapper.fromEntityToDomain(entityStep)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }

  @Test
  fun `should update entity from domain without updating JPA managed fields`() {
    // Given
    val id = UUID.randomUUID()
    val createdAt = Instant.now().minusSeconds(60)
    val createdBy = "a.user.id"
    val updatedAt = Instant.now()
    val updatedBy = "another.user.id"

    val existingEntity = aValidStepEntity(
      id = id,
      createdAt = createdAt,
      createdBy = createdBy,
      updatedAt = updatedAt,
      updatedBy = updatedBy,
      title = "Improve communication skills",
      targetDate = LocalDate.now().plusMonths(6),
    )

    val updateTitle = "Improve communication skills within 3 months"
    val updatedTargetDate = LocalDate.now().plusMonths(3)
    val domainGoal = aValidStep(
      title = updateTitle,
      targetDate = updatedTargetDate,
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
      .hasTargetDate(updatedTargetDate)
  }
}
