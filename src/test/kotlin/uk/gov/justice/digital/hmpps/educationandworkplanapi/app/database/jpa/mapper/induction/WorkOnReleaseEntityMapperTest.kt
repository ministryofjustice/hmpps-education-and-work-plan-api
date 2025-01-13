package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidCreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork as AffectAbilityToWorkDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork as AffectAbilityToWorkEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork as HopingToWorkEntity

class WorkOnReleaseEntityMapperTest {

  private val mapper = WorkOnReleaseEntityMapper()

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createWorkOnReleaseDto = aValidCreateWorkOnReleaseDto(
      hopingToWork = HopingToWorkDomain.NO,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Work is for fools and horses",
    )
    val expected = aValidWorkOnReleaseEntity(
      hopingToWork = HopingToWorkEntity.NO,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkEntity.OTHER),
      affectAbilityToWorkOther = "Work is for fools and horses",
    )

    // When
    val actual = mapper.fromCreateDtoToEntity(createWorkOnReleaseDto)

    // Then
    assertThat(actual)
      .doesNotHaveJpaManagedFieldsPopulated()
      .hasAReference()
      .usingRecursiveComparison()
      .ignoringFieldsMatchingRegexes(".*reference")
      .isEqualTo(expected)
  }

  @Test
  fun `should map from entity to domain`() {
    // Given
    val workOnReleaseEntity = aValidWorkOnReleaseEntityWithJpaFieldsPopulated()
    val expectedWorkOnReleaseEntity = aValidWorkOnRelease(
      reference = workOnReleaseEntity.reference!!,
      hopingToWork = HopingToWorkDomain.NO,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Negative attitude",
      createdAt = workOnReleaseEntity.createdAt!!,
      createdAtPrison = workOnReleaseEntity.createdAtPrison!!,
      createdBy = workOnReleaseEntity.createdBy!!,
      lastUpdatedAt = workOnReleaseEntity.updatedAt!!,
      lastUpdatedAtPrison = workOnReleaseEntity.updatedAtPrison!!,
      lastUpdatedBy = workOnReleaseEntity.updatedBy!!,
    )

    // When
    val actual = mapper.fromEntityToDomain(workOnReleaseEntity)

    // Then
    assertThat(actual).isEqualTo(expectedWorkOnReleaseEntity)
  }
}
