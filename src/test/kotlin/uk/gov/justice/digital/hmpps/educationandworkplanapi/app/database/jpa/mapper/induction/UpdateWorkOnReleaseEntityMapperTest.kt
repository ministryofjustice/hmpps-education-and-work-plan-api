package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.aValidUpdateWorkOnReleaseDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidWorkOnReleaseEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.AffectAbilityToWork as AffectAbilityToWorkDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HopingToWork as HopingToWorkDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.AffectAbilityToWork as AffectAbilityToWorkEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HopingToWork as HopingToWorkEntity

class UpdateWorkOnReleaseEntityMapperTest {

  private val mapper = WorkOnReleaseEntityMapper()

  @Test
  fun `should update existing work on release values`() {
    // Given
    val workOnReleaseReference = UUID.randomUUID()
    val existingWorkOnReleaseEntity = aValidWorkOnReleaseEntity(
      reference = workOnReleaseReference,
      hopingToWork = HopingToWorkEntity.NO,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkEntity.OTHER),
      affectAbilityToWorkOther = "Negative attitude",
    )

    val updatedWorkOnReleaseDto = aValidUpdateWorkOnReleaseDto(
      reference = workOnReleaseReference,
      hopingToWork = HopingToWorkDomain.NOT_SURE,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkDomain.CARING_RESPONSIBILITIES),
      affectAbilityToWorkOther = null,
      prisonId = "MDI",
    )

    val expectedEntity = existingWorkOnReleaseEntity.deepCopy().apply {
      id
      reference = reference
      hopingToWork = HopingToWorkEntity.NOT_SURE
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkEntity.CARING_RESPONSIBILITIES)
      affectAbilityToWorkOther = null
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateExistingEntityFromDto(existingWorkOnReleaseEntity, updatedWorkOnReleaseDto)

    // Then
    assertThat(existingWorkOnReleaseEntity).isEqualToComparingAllFields(expectedEntity)
  }

  @Test
  fun `should add new work on release values`() {
    // Given
    val workOnReleaseReference = UUID.randomUUID()
    val existingWorkOnReleaseEntity = aValidWorkOnReleaseEntity(
      reference = workOnReleaseReference,
      hopingToWork = HopingToWorkEntity.NO,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkEntity.OTHER),
      affectAbilityToWorkOther = "Negative attitude",
    )

    val updatedWorkOnReleaseDto = aValidUpdateWorkOnReleaseDto(
      reference = workOnReleaseReference,
      hopingToWork = HopingToWorkDomain.NOT_SURE,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkDomain.OTHER, AffectAbilityToWorkDomain.CARING_RESPONSIBILITIES),
      affectAbilityToWorkOther = "Lacking confidence",
      prisonId = "MDI",
    )

    val expectedEntity = existingWorkOnReleaseEntity.deepCopy().apply {
      id
      reference = reference
      hopingToWork = HopingToWorkEntity.NOT_SURE
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkEntity.OTHER, AffectAbilityToWorkEntity.CARING_RESPONSIBILITIES)
      affectAbilityToWorkOther = "Lacking confidence"
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateExistingEntityFromDto(existingWorkOnReleaseEntity, updatedWorkOnReleaseDto)

    // Then
    assertThat(existingWorkOnReleaseEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
  }

  @Test
  fun `should remove work on release values`() {
    // Given
    val workOnReleaseReference = UUID.randomUUID()
    val existingWorkOnReleaseEntity = aValidWorkOnReleaseEntity(
      reference = workOnReleaseReference,
      hopingToWork = HopingToWorkEntity.NO,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkEntity.OTHER, AffectAbilityToWorkEntity.CARING_RESPONSIBILITIES),
      affectAbilityToWorkOther = "Negative attitude",
    )

    val updatedWorkOnReleaseDto = aValidUpdateWorkOnReleaseDto(
      reference = workOnReleaseReference,
      hopingToWork = HopingToWorkDomain.NOT_SURE,
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkDomain.CARING_RESPONSIBILITIES),
      affectAbilityToWorkOther = null,
      prisonId = "MDI",
    )

    val expectedEntity = existingWorkOnReleaseEntity.deepCopy().apply {
      id
      reference = reference
      hopingToWork = HopingToWorkEntity.NOT_SURE
      affectAbilityToWork = mutableListOf(AffectAbilityToWorkEntity.CARING_RESPONSIBILITIES)
      affectAbilityToWorkOther = null
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateExistingEntityFromDto(existingWorkOnReleaseEntity, updatedWorkOnReleaseDto)

    // Then
    assertThat(existingWorkOnReleaseEntity).isEqualToComparingAllFields(expectedEntity)
  }
}
