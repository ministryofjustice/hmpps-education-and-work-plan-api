package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.aValidStepEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.TargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.aValidStep
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.StepStatus as EntityStatus
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.TargetDateRange as EntityTargetDateRange
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.goal.StepStatus as DomainStatus

class StepEntityMapperTest {

  private val mapper = StepEntityMapperImpl()

  @Test
  fun `should map from domain to entity`() {
    // Given
    val domainStep = aValidStep(
      reference = UUID.randomUUID(),
      title = "Book communication skills course",
      targetDateRange = TargetDateRange.ZERO_TO_THREE_MONTHS,
      status = DomainStatus.ACTIVE,
      sequenceNumber = 1,
    )

    val expected = aValidStepEntity(
      reference = domainStep.reference,
      title = "Book communication skills course",
      targetDateRange = EntityTargetDateRange.ZERO_TO_THREE_MONTHS,
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
      targetDateRange = EntityTargetDateRange.THREE_TO_SIX_MONTHS,
      status = EntityStatus.ACTIVE,
      sequenceNumber = 1,
    )

    val expected = aValidStep(
      reference = entityStep.reference!!,
      title = "Book communication skills course",
      targetDateRange = TargetDateRange.THREE_TO_SIX_MONTHS,
      status = DomainStatus.ACTIVE,
      sequenceNumber = 1,
    )

    // When
    val actual = mapper.fromEntityToDomain(entityStep)

    // Then
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
  }
}
