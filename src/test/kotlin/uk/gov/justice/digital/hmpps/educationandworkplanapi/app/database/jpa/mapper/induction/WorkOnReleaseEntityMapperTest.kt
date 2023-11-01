package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidWorkOnRelease
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidCreateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork as AffectAbilityToWorkEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork as HopingToWorkEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.NotHopingToWorkReason as NotHopingToWorkReasonEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.AffectAbilityToWork as AffectAbilityToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.NotHopingToWorkReason as NotHopingToWorkReasonDomain

class WorkOnReleaseEntityMapperTest {

  private val mapper = WorkOnReleaseEntityMapperImpl()

  @Test
  fun `should map from dto to entity`() {
    // Given
    val createWorkOnReleaseDto = aValidCreateWorkOnReleaseDto(
      hopingToWork = HopingToWorkDomain.NO,
      notHopingToWorkReasons = listOf(NotHopingToWorkReasonDomain.OTHER),
      notHopingToWorkOtherReason = "Crime pays",
      affectAbilityToWork = listOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Work is for fools and horses",
    )
    val expected = aValidWorkOnReleaseEntity(
      hopingToWork = HopingToWorkEntity.NO,
      notHopingToWorkReasons = listOf(NotHopingToWorkReasonEntity.OTHER),
      notHopingToWorkOtherReason = "Crime pays",
      affectAbilityToWork = listOf(AffectAbilityToWorkEntity.OTHER),
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
      notHopingToWorkReasons = listOf(NotHopingToWorkReasonDomain.OTHER),
      notHopingToWorkOtherReason = "No motivation",
      affectAbilityToWork = listOf(AffectAbilityToWorkDomain.OTHER),
      affectAbilityToWorkOther = "Negative attitude",
      createdAt = workOnReleaseEntity.createdAt!!,
      createdAtPrison = workOnReleaseEntity.createdAtPrison!!,
      createdBy = workOnReleaseEntity.createdBy!!,
      createdByDisplayName = workOnReleaseEntity.createdByDisplayName!!,
      lastUpdatedAt = workOnReleaseEntity.updatedAt!!,
      lastUpdatedAtPrison = workOnReleaseEntity.updatedAtPrison!!,
      lastUpdatedBy = workOnReleaseEntity.updatedBy!!,
      lastUpdatedByDisplayName = workOnReleaseEntity.updatedByDisplayName!!,
    )

    // When
    val actual = mapper.fromEntityToDomain(workOnReleaseEntity)

    // Then
    assertThat(actual).isEqualTo(expectedWorkOnReleaseEntity)
  }
}
