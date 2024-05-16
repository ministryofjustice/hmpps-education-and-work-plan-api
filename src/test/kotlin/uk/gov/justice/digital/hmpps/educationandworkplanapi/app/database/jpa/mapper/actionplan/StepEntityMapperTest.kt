package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.actionplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.aValidReference
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus
import uk.gov.justice.digital.hmpps.domain.personallearningplan.aValidStep
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidCreateStepDto
import uk.gov.justice.digital.hmpps.domain.personallearningplan.dto.aValidUpdateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.assertThat
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.personallearningplan.StepStatus as DomainStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.actionplan.StepStatus as EntityStatus

class StepEntityMapperTest {

  private val mapper = StepEntityMapperImpl()

  @Test
  fun `should map from CreateStepDto to entity`() {
    // Given
    val createStepDto = aValidCreateStepDto(
      title = "Book communication skills course",
      status = DomainStatus.ACTIVE,
      sequenceNumber = 1,
    )

    val expected = aValidStepEntity(
      title = "Book communication skills course",
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
    val actual = mapper.fromDtoToEntity(createStepDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFields("reference")
      .isEqualTo(expected)
  }

  @Test
  fun `should map from UpdateStepDto to entity`() {
    // Given
    val updateStepDto = aValidUpdateStepDto(
      reference = UUID.randomUUID(),
      title = "Book communication skills course",
      status = DomainStatus.ACTIVE,
      sequenceNumber = 1,
    )

    val expected = aValidStepEntity(
      reference = updateStepDto.reference!!,
      title = "Book communication skills course",
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
    val actual = mapper.fromDtoToEntity(updateStepDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison().isEqualTo(expected)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val entityStep = aValidStepEntity(
      id = null,
      reference = UUID.randomUUID(),
      title = "Book communication skills course",
      status = EntityStatus.ACTIVE,
      sequenceNumber = 1,
    )

    val expected = aValidStep(
      reference = entityStep.reference!!,
      title = "Book communication skills course",
      status = DomainStatus.ACTIVE,
      sequenceNumber = 1,
    )

    // When
    val actual = mapper.fromEntityToDomain(entityStep)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }

  @Test
  fun `should updateEntityFromDto`() {
    // Given
    val stepEntity = aValidStepEntity(
      title = "Book course",
      status = EntityStatus.NOT_STARTED,
      sequenceNumber = 1,
    )

    val updateStepDto = aValidUpdateStepDto(
      title = "Book the course with the instructor",
      status = StepStatus.ACTIVE,
      sequenceNumber = 2,
    )

    // When
    mapper.updateEntityFromDto(stepEntity, updateStepDto)

    // Then
    assertThat(stepEntity)
      .hasTitle("Book the course with the instructor")
      .hasStatus(EntityStatus.ACTIVE)
      .hasSequenceNumber(2)
  }

  @Test
  fun `should map from UpdateStepDto with reference to domain`() {
    // Given
    val reference = aValidReference()
    val updateStepDto = aValidUpdateStepDto(reference = reference)

    val expected = aValidStep(
      reference = reference,
      title = updateStepDto.title,
      status = updateStepDto.status,
      sequenceNumber = updateStepDto.sequenceNumber,
    )

    // When
    val actual = mapper.fromDtoToDomain(updateStepDto)

    // Then
    assertThat(actual).isEqualTo(expected)
  }

  @Test
  fun `should map from UpdateStepDto without reference to domain`() {
    // Given
    val updateStepDto = aValidUpdateStepDto(reference = null)

    val expected = aValidStep(
      title = updateStepDto.title,
      status = updateStepDto.status,
      sequenceNumber = updateStepDto.sequenceNumber,
    )

    // When
    val actual = mapper.fromDtoToDomain(updateStepDto)

    // Then
    assertThat(actual)
      .usingRecursiveComparison()
      .ignoringFields("reference")
      .isEqualTo(expected)
  }

  @Test
  fun `should map from domain to entity`() {
    // Given
    val reference = aValidReference()
    val step = aValidStep(
      reference = reference,
      status = DomainStatus.ACTIVE,
    )

    val expected = aValidStepEntity(
      reference = reference,
      title = step.title,
      status = EntityStatus.ACTIVE,
      sequenceNumber = step.sequenceNumber,
    )

    // When
    val actual = mapper.fromDomainToEntity(step)

    // Then
    assertThat(actual).isEqualToIgnoringJpaManagedFields(expected)
  }

  @Test
  fun `should update entity from domain`() {
    // Given
    val reference = aValidReference()
    val stepEntity = aValidStepEntity(
      reference = reference,
      title = "Book course",
      status = EntityStatus.NOT_STARTED,
      sequenceNumber = 1,
    )

    val step = aValidStep(
      reference = reference,
      title = "Book course before December",
      status = DomainStatus.ACTIVE,
      sequenceNumber = 2,
    )

    // When
    mapper.updateEntityFromDomain(stepEntity, step)

    // Then
    assertThat(stepEntity)
      .hasTitle("Book course before December")
      .hasStatus(EntityStatus.ACTIVE)
      .hasSequenceNumber(2)
      .hasReference(reference)
  }
}
