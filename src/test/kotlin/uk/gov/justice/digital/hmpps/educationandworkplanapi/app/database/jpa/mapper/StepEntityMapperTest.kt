package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidCreateStepDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.dto.aValidUpdateStepDto
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus as EntityStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus as DomainStatus

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
}
