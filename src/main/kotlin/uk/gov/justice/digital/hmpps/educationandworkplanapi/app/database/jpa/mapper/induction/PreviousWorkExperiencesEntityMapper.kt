package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.PreviousWorkExperiences
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperience
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdatePreviousWorkExperiencesDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.PreviousWorkExperiencesEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.HasWorkedBefore as HasWorkedBeforeDomain
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkExperienceType as WorkExperienceTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.HasWorkedBefore as HasWorkedBeforeEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkExperienceType as WorkExperienceTypeEntity

@Component
class PreviousWorkExperiencesEntityMapper(
  private val workExperienceEntityMapper: WorkExperienceEntityMapper,
  private val entityListManager: InductionEntityListManager<WorkExperienceEntity, WorkExperience>,
) {

  fun fromCreateDtoToEntity(dto: CreatePreviousWorkExperiencesDto): PreviousWorkExperiencesEntity =
    with(dto) {
      PreviousWorkExperiencesEntity(
        reference = UUID.randomUUID(),
        hasWorkedBefore = toHasWorkedBefore(hasWorkedBefore),
        hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ).apply {
        addNewExperiences(dto.experiences, this)
      }
    }

  fun fromEntityToDomain(persistedEntity: PreviousWorkExperiencesEntity?): PreviousWorkExperiences? =
    persistedEntity?.let {
      PreviousWorkExperiences(
        reference = it.reference,
        hasWorkedBefore = toHasWorkedBefore(it.hasWorkedBefore),
        hasWorkedBeforeNotRelevantReason = it.hasWorkedBeforeNotRelevantReason,
        experiences = it.experiences.map { workExperienceEntityMapper.fromEntityToDomain(it) },
        createdBy = it.createdBy!!,
        createdAt = it.createdAt!!,
        createdAtPrison = it.createdAtPrison,
        lastUpdatedBy = it.updatedBy!!,
        lastUpdatedAt = it.updatedAt!!,
        lastUpdatedAtPrison = it.updatedAtPrison,
      )
    }

  fun updateExistingEntityFromDto(entity: PreviousWorkExperiencesEntity, dto: UpdatePreviousWorkExperiencesDto) =
    with(entity) {
      hasWorkedBefore = toHasWorkedBefore(dto.hasWorkedBefore)
      hasWorkedBeforeNotRelevantReason = dto.hasWorkedBeforeNotRelevantReason
      updatedAtPrison = dto.prisonId

      val existingExperiences = experiences
      val updatedExperiences = dto.experiences
      entityListManager.updateExisting(existingExperiences, updatedExperiences, workExperienceEntityMapper)
      entityListManager.addNew(entity, existingExperiences, updatedExperiences, workExperienceEntityMapper)
      entityListManager.deleteRemoved(existingExperiences, updatedExperiences)
    }

  fun fromUpdateDtoToNewEntity(dto: UpdatePreviousWorkExperiencesDto): PreviousWorkExperiencesEntity =
    with(dto) {
      PreviousWorkExperiencesEntity(
        reference = UUID.randomUUID(),
        hasWorkedBefore = toHasWorkedBefore(hasWorkedBefore),
        hasWorkedBeforeNotRelevantReason = hasWorkedBeforeNotRelevantReason,
        createdAtPrison = prisonId,
        updatedAtPrison = prisonId,
      ).apply {
        addNewExperiences(dto.experiences, this)
      }
    }

  private fun addNewExperiences(experiences: List<WorkExperience>, entity: PreviousWorkExperiencesEntity) {
    experiences.forEach {
      entity.addChild(
        workExperienceEntityMapper.fromDomainToEntity(it),
        entity.experiences,
      )
    }
  }

  private fun toHasWorkedBefore(hasWorkedBefore: HasWorkedBeforeEntity): HasWorkedBeforeDomain =
    when (hasWorkedBefore) {
      HasWorkedBeforeEntity.YES -> HasWorkedBeforeDomain.YES
      HasWorkedBeforeEntity.NO -> HasWorkedBeforeDomain.NO
      HasWorkedBeforeEntity.NOT_RELEVANT -> HasWorkedBeforeDomain.NOT_RELEVANT
    }

  private fun toHasWorkedBefore(hasWorkedBefore: HasWorkedBeforeDomain): HasWorkedBeforeEntity =
    when (hasWorkedBefore) {
      HasWorkedBeforeDomain.YES -> HasWorkedBeforeEntity.YES
      HasWorkedBeforeDomain.NO -> HasWorkedBeforeEntity.NO
      HasWorkedBeforeDomain.NOT_RELEVANT -> HasWorkedBeforeEntity.NOT_RELEVANT
    }
}

@Component
class WorkExperienceEntityMapper : KeyAwareEntityMapper<WorkExperienceEntity, WorkExperience> {
  override fun fromDomainToEntity(domain: WorkExperience): WorkExperienceEntity =
    with(domain) {
      WorkExperienceEntity(
        reference = UUID.randomUUID(),
        experienceType = toExperienceType(experienceType),
        experienceTypeOther = experienceTypeOther,
        role = role,
        details = details,
      )
    }

  fun fromEntityToDomain(persistedEntity: WorkExperienceEntity): WorkExperience =
    with(persistedEntity) {
      WorkExperience(
        experienceType = toExperienceType(experienceType),
        experienceTypeOther = experienceTypeOther,
        role = role,
        details = details,
      )
    }

  override fun updateEntityFromDomain(entity: WorkExperienceEntity, domain: WorkExperience) =
    with(entity) {
      experienceType = toExperienceType(domain.experienceType)
      experienceTypeOther = domain.experienceTypeOther
      role = domain.role
      details = domain.details
    }

  private fun toExperienceType(experienceType: WorkExperienceTypeEntity): WorkExperienceTypeDomain =
    when (experienceType) {
      WorkExperienceTypeEntity.BEAUTY -> WorkExperienceTypeDomain.BEAUTY
      WorkExperienceTypeEntity.OUTDOOR -> WorkExperienceTypeDomain.OUTDOOR
      WorkExperienceTypeEntity.CONSTRUCTION -> WorkExperienceTypeDomain.CONSTRUCTION
      WorkExperienceTypeEntity.DRIVING -> WorkExperienceTypeDomain.DRIVING
      WorkExperienceTypeEntity.HOSPITALITY -> WorkExperienceTypeDomain.HOSPITALITY
      WorkExperienceTypeEntity.TECHNICAL -> WorkExperienceTypeDomain.TECHNICAL
      WorkExperienceTypeEntity.MANUFACTURING -> WorkExperienceTypeDomain.MANUFACTURING
      WorkExperienceTypeEntity.OFFICE -> WorkExperienceTypeDomain.OFFICE
      WorkExperienceTypeEntity.RETAIL -> WorkExperienceTypeDomain.RETAIL
      WorkExperienceTypeEntity.SPORTS -> WorkExperienceTypeDomain.SPORTS
      WorkExperienceTypeEntity.WAREHOUSING -> WorkExperienceTypeDomain.WAREHOUSING
      WorkExperienceTypeEntity.WASTE_MANAGEMENT -> WorkExperienceTypeDomain.WASTE_MANAGEMENT
      WorkExperienceTypeEntity.EDUCATION_TRAINING -> WorkExperienceTypeDomain.EDUCATION_TRAINING
      WorkExperienceTypeEntity.CLEANING_AND_MAINTENANCE -> WorkExperienceTypeDomain.CLEANING_AND_MAINTENANCE
      WorkExperienceTypeEntity.OTHER -> WorkExperienceTypeDomain.OTHER
    }

  private fun toExperienceType(experienceType: WorkExperienceTypeDomain): WorkExperienceTypeEntity =
    when (experienceType) {
      WorkExperienceTypeDomain.BEAUTY -> WorkExperienceTypeEntity.BEAUTY
      WorkExperienceTypeDomain.OUTDOOR -> WorkExperienceTypeEntity.OUTDOOR
      WorkExperienceTypeDomain.CONSTRUCTION -> WorkExperienceTypeEntity.CONSTRUCTION
      WorkExperienceTypeDomain.DRIVING -> WorkExperienceTypeEntity.DRIVING
      WorkExperienceTypeDomain.HOSPITALITY -> WorkExperienceTypeEntity.HOSPITALITY
      WorkExperienceTypeDomain.TECHNICAL -> WorkExperienceTypeEntity.TECHNICAL
      WorkExperienceTypeDomain.MANUFACTURING -> WorkExperienceTypeEntity.MANUFACTURING
      WorkExperienceTypeDomain.OFFICE -> WorkExperienceTypeEntity.OFFICE
      WorkExperienceTypeDomain.RETAIL -> WorkExperienceTypeEntity.RETAIL
      WorkExperienceTypeDomain.SPORTS -> WorkExperienceTypeEntity.SPORTS
      WorkExperienceTypeDomain.WAREHOUSING -> WorkExperienceTypeEntity.WAREHOUSING
      WorkExperienceTypeDomain.WASTE_MANAGEMENT -> WorkExperienceTypeEntity.WASTE_MANAGEMENT
      WorkExperienceTypeDomain.EDUCATION_TRAINING -> WorkExperienceTypeEntity.EDUCATION_TRAINING
      WorkExperienceTypeDomain.CLEANING_AND_MAINTENANCE -> WorkExperienceTypeEntity.CLEANING_AND_MAINTENANCE
      WorkExperienceTypeDomain.OTHER -> WorkExperienceTypeEntity.OTHER
    }
}
