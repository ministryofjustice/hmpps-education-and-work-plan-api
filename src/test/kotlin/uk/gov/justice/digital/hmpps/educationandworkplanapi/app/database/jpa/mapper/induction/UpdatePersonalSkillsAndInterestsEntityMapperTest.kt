package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalInterestEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.assertThat
import uk.gov.justice.digital.hmpps.educationandworkplanapi.deepCopy
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.PersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalInterest
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.aValidPersonalSkill
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.dto.aValidUpdatePersonalSkillsAndInterestsDto
import java.util.UUID
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.InterestType as InterestTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.SkillType as SkillTypeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.InterestType as InterestTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.SkillType as SkillTypeDomain

class UpdatePersonalSkillsAndInterestsEntityMapperTest {

  private val mapper = PersonalSkillsAndInterestsEntityMapperImpl().also {
    PersonalSkillsAndInterestsEntityMapper::class.java.getDeclaredField("personalSkillEntityMapper").apply {
      isAccessible = true
      set(it, PersonalSkillEntityMapperImpl())
    }

    PersonalSkillsAndInterestsEntityMapper::class.java.getDeclaredField("personalInterestEntityMapper").apply {
      isAccessible = true
      set(it, PersonalInterestEntityMapperImpl())
    }

    PersonalSkillsAndInterestsEntityMapper::class.java.getDeclaredField("personalSkillEntityListManager").apply {
      isAccessible = true
      set(it, InductionEntityListManager<PersonalSkillEntity, PersonalSkill>())
    }

    PersonalSkillsAndInterestsEntityMapper::class.java.getDeclaredField("personalInterestEntityListManager").apply {
      isAccessible = true
      set(it, InductionEntityListManager<PersonalInterestEntity, PersonalInterest>())
    }
  }

  @Test
  fun `should update existing skills and interests`() {
    // Given
    val skillReference = UUID.randomUUID()
    val existingSkillEntity = aValidPersonalSkillEntity(
      reference = skillReference,
      skillType = SkillTypeEntity.OTHER,
      skillTypeOther = "Too many skills to mention",
    )
    val interestReference = UUID.randomUUID()
    val existingInterestEntity = aValidPersonalInterestEntity(
      reference = interestReference,
      interestType = InterestTypeEntity.OTHER,
      interestTypeOther = "Lots of varied interests",
    )
    val skillsAndInterestsReference = UUID.randomUUID()
    val existingSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated(
      reference = skillsAndInterestsReference,
      skills = mutableListOf(existingSkillEntity),
      interests = mutableListOf(existingInterestEntity),
    )
    val initialUpdatedAt = existingSkillsAndInterestsEntity.updatedAt!!

    val updatedSkill = aValidPersonalSkill(
      skillType = SkillTypeDomain.OTHER,
      skillTypeOther = "Not that many skills actually",
    )
    val updatedInterest = aValidPersonalInterest(
      interestType = InterestTypeDomain.OTHER,
      interestTypeOther = "Not such varied interests actually",
    )
    val updateDto = aValidUpdatePersonalSkillsAndInterestsDto(
      reference = skillsAndInterestsReference,
      skills = listOf(updatedSkill),
      interests = listOf(updatedInterest),
      prisonId = "MDI",
    )

    val expectedEntity = existingSkillsAndInterestsEntity.deepCopy().apply {
      id
      reference = reference
      skills = mutableListOf(
        existingSkillEntity.deepCopy().apply {
          skillType = SkillTypeEntity.OTHER
          skillTypeOther = "Not that many skills actually"
        },
      )
      interests = mutableListOf(
        existingInterestEntity.deepCopy().apply {
          interestType = InterestTypeEntity.OTHER
          interestTypeOther = "Not such varied interests actually"
        },
      )
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateEntityFromDto(existingSkillsAndInterestsEntity, updateDto)

    // Then
    assertThat(existingSkillsAndInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
    // TODO RR-469 - fix updatedAt timestamp
    // assertThat(existingSkillsAndInterestsEntity).wasUpdatedAfter(initialUpdatedAt)
  }

  @Test
  fun `should add new skills and interests`() {
    // Given
    val skillReference = UUID.randomUUID()
    val existingSkillEntity = aValidPersonalSkillEntity(
      reference = skillReference,
      skillType = SkillTypeEntity.OTHER,
      skillTypeOther = "Too many skills to mention",
    )
    val interestReference = UUID.randomUUID()
    val existingInterestEntity = aValidPersonalInterestEntity(
      reference = interestReference,
      interestType = InterestTypeEntity.OTHER,
      interestTypeOther = "Lots of varied interests",
    )
    val skillsAndInterestsReference = UUID.randomUUID()
    val existingSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated(
      reference = skillsAndInterestsReference,
      skills = mutableListOf(existingSkillEntity),
      interests = mutableListOf(existingInterestEntity),
    )
    val initialUpdatedAt = existingSkillsAndInterestsEntity.updatedAt!!

    val existingSkill = aValidPersonalSkill(
      skillType = SkillTypeDomain.OTHER,
      skillTypeOther = "Too many skills to mention",
    )
    val existingInterest = aValidPersonalInterest(
      interestType = InterestTypeDomain.OTHER,
      interestTypeOther = "Lots of varied interests",
    )
    val newSkill = aValidPersonalSkill(
      skillType = SkillTypeDomain.COMMUNICATION,
      skillTypeOther = null,
    )
    val newInterest = aValidPersonalInterest(
      interestType = InterestTypeDomain.CRAFTS,
      interestTypeOther = null,
    )
    val updateDto = aValidUpdatePersonalSkillsAndInterestsDto(
      reference = skillsAndInterestsReference,
      skills = listOf(existingSkill, newSkill),
      interests = listOf(existingInterest, newInterest),
      prisonId = "MDI",
    )

    val expectedEntity = existingSkillsAndInterestsEntity.deepCopy().apply {
      id
      reference = reference
      skills = mutableListOf(
        aValidPersonalSkillEntity(
          skillType = SkillTypeEntity.OTHER,
          skillTypeOther = "Too many skills to mention",
        ),
        aValidPersonalSkillEntity(
          skillType = SkillTypeEntity.COMMUNICATION,
          skillTypeOther = null,
        ),
      )
      interests = mutableListOf(
        aValidPersonalInterestEntity(
          interestType = InterestTypeEntity.OTHER,
          interestTypeOther = "Lots of varied interests",
        ),
        aValidPersonalInterestEntity(
          interestType = InterestTypeEntity.CRAFTS,
          interestTypeOther = null,
        ),
      )
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateEntityFromDto(existingSkillsAndInterestsEntity, updateDto)

    // Then
    assertThat(existingSkillsAndInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
    // TODO RR-469 - fix updatedAt timestamp
    // assertThat(existingSkillsAndInterestsEntity).wasUpdatedAfter(initialUpdatedAt)
  }

  @Test
  fun `should remove skills and interests`() {
    // Given
    val skillReference = UUID.randomUUID()
    val firstSkillEntity = aValidPersonalSkillEntity(
      reference = skillReference,
      skillType = SkillTypeEntity.RESILIENCE,
      skillTypeOther = null,
    )
    val secondSkillEntity = aValidPersonalSkillEntity(
      skillType = SkillTypeEntity.COMMUNICATION,
      skillTypeOther = null,
    )
    val interestReference = UUID.randomUUID()
    val firstInterestEntity = aValidPersonalInterestEntity(
      reference = interestReference,
      interestType = InterestTypeEntity.CRAFTS,
      interestTypeOther = null,
    )
    val secondInterestEntity = aValidPersonalInterestEntity(
      interestType = InterestTypeEntity.NATURE_AND_ANIMALS,
      interestTypeOther = null,
    )
    val skillsAndInterestsReference = UUID.randomUUID()
    val existingSkillsAndInterestsEntity = aValidPersonalSkillsAndInterestsEntityWithJpaFieldsPopulated(
      reference = skillsAndInterestsReference,
      skills = mutableListOf(firstSkillEntity, secondSkillEntity),
      interests = mutableListOf(firstInterestEntity, secondInterestEntity),
    )
    val initialUpdatedAt = existingSkillsAndInterestsEntity.updatedAt!!

    val existingSkill = aValidPersonalSkill(
      skillType = SkillTypeDomain.RESILIENCE,
      skillTypeOther = null,
    )
    val existingInterest = aValidPersonalInterest(
      interestType = InterestTypeDomain.CRAFTS,
      interestTypeOther = null,
    )
    val updateDto = aValidUpdatePersonalSkillsAndInterestsDto(
      reference = skillsAndInterestsReference,
      skills = listOf(existingSkill),
      interests = listOf(existingInterest),
      prisonId = "MDI",
    )

    val expectedEntity = existingSkillsAndInterestsEntity.deepCopy().apply {
      id
      reference = reference
      skills = mutableListOf(firstSkillEntity)
      interests = mutableListOf(firstInterestEntity)
      createdAtPrison = "BXI"
      updatedAtPrison = "MDI"
    }

    // When
    mapper.updateEntityFromDto(existingSkillsAndInterestsEntity, updateDto)

    // Then
    assertThat(existingSkillsAndInterestsEntity).isEqualToIgnoringInternallyManagedFields(expectedEntity)
    // TODO RR-469 - fix updatedAt timestamp
    // assertThat(existingSkillsAndInterestsEntity).wasUpdatedAfter(initialUpdatedAt)
  }
}
