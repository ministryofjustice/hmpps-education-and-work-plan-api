package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.FutureWorkInterests
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterest
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.CreateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.dto.UpdateFutureWorkInterestsDto
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.FutureWorkInterestsEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestEntity
import java.util.UUID
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.WorkInterestType as WorkInterestTypeDomain
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.WorkInterestType as WorkInterestTypeEntity

@Component
class FutureWorkInterestsEntityMapper(
  private val workInterestEntityMapper: WorkInterestEntityMapper,
  private val entityListManager: InductionEntityListManager<WorkInterestEntity, WorkInterest>,
) {
  fun fromCreateDtoToEntity(dto: CreateFutureWorkInterestsDto): FutureWorkInterestsEntity = with(dto) {
    FutureWorkInterestsEntity(
      reference = UUID.randomUUID(),
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    ).apply {
      addNewInterests(dto.interests, this)
    }
  }

  fun fromEntityToDomain(entity: FutureWorkInterestsEntity?): FutureWorkInterests? = entity?.let {
    FutureWorkInterests(
      reference = it.reference,
      interests = it.interests.map { workInterestEntityMapper.fromEntityToDomain(it) },
      createdBy = it.createdBy!!,
      createdAt = it.createdAt!!,
      createdAtPrison = it.createdAtPrison,
      lastUpdatedBy = it.updatedBy!!,
      lastUpdatedAt = it.updatedAt!!,
      lastUpdatedAtPrison = it.updatedAtPrison,
    )
  }

  fun updateExistingEntityFromDto(entity: FutureWorkInterestsEntity, dto: UpdateFutureWorkInterestsDto) = with(entity) {
    updatedAtPrison = dto.prisonId

    val existingInterests = entity.interests
    val updatedInterests = dto.interests
    entityListManager.updateExisting(existingInterests, updatedInterests, workInterestEntityMapper)
    entityListManager.addNew(entity, existingInterests, updatedInterests, workInterestEntityMapper)
    entityListManager.deleteRemoved(existingInterests, updatedInterests)
  }

  fun fromUpdateDtoToNewEntity(dto: UpdateFutureWorkInterestsDto): FutureWorkInterestsEntity = with(dto) {
    FutureWorkInterestsEntity(
      reference = UUID.randomUUID(),
      createdAtPrison = prisonId,
      updatedAtPrison = prisonId,
    ).apply {
      addNewInterests(dto.interests, this)
    }
  }

  private fun addNewInterests(interests: List<WorkInterest>, entity: FutureWorkInterestsEntity) {
    interests.forEach {
      entity.addChild(
        workInterestEntityMapper.fromDomainToEntity(it),
        entity.interests,
      )
    }
  }
}

@Component
class WorkInterestEntityMapper : KeyAwareEntityMapper<WorkInterestEntity, WorkInterest> {
  override fun fromDomainToEntity(domain: WorkInterest): WorkInterestEntity = with(domain) {
    WorkInterestEntity(
      reference = UUID.randomUUID(),
      workType = toWorkType(workType),
      workTypeOther = workTypeOther,
      role = role,
    )
  }

  fun fromEntityToDomain(entity: WorkInterestEntity): WorkInterest = with(entity) {
    WorkInterest(
      workType = toWorkType(workType),
      workTypeOther = workTypeOther,
      role = role,
    )
  }

  override fun updateEntityFromDomain(entity: WorkInterestEntity, domain: WorkInterest) = with(entity) {
    workType = toWorkType(domain.workType)
    workTypeOther = domain.workTypeOther
    role = domain.role
  }

  private fun toWorkType(workType: WorkInterestTypeEntity): WorkInterestTypeDomain = when (workType) {
    WorkInterestTypeEntity.OUTDOOR -> WorkInterestTypeDomain.OUTDOOR
    WorkInterestTypeEntity.CONSTRUCTION -> WorkInterestTypeDomain.CONSTRUCTION
    WorkInterestTypeEntity.DRIVING -> WorkInterestTypeDomain.DRIVING
    WorkInterestTypeEntity.BEAUTY -> WorkInterestTypeDomain.BEAUTY
    WorkInterestTypeEntity.HOSPITALITY -> WorkInterestTypeDomain.HOSPITALITY
    WorkInterestTypeEntity.TECHNICAL -> WorkInterestTypeDomain.TECHNICAL
    WorkInterestTypeEntity.MANUFACTURING -> WorkInterestTypeDomain.MANUFACTURING
    WorkInterestTypeEntity.OFFICE -> WorkInterestTypeDomain.OFFICE
    WorkInterestTypeEntity.RETAIL -> WorkInterestTypeDomain.RETAIL
    WorkInterestTypeEntity.SPORTS -> WorkInterestTypeDomain.SPORTS
    WorkInterestTypeEntity.WAREHOUSING -> WorkInterestTypeDomain.WAREHOUSING
    WorkInterestTypeEntity.WASTE_MANAGEMENT -> WorkInterestTypeDomain.WASTE_MANAGEMENT
    WorkInterestTypeEntity.EDUCATION_TRAINING -> WorkInterestTypeDomain.EDUCATION_TRAINING
    WorkInterestTypeEntity.CLEANING_AND_MAINTENANCE -> WorkInterestTypeDomain.CLEANING_AND_MAINTENANCE
    WorkInterestTypeEntity.OTHER -> WorkInterestTypeDomain.OTHER
  }

  private fun toWorkType(workType: WorkInterestTypeDomain): WorkInterestTypeEntity = when (workType) {
    WorkInterestTypeDomain.OUTDOOR -> WorkInterestTypeEntity.OUTDOOR
    WorkInterestTypeDomain.CONSTRUCTION -> WorkInterestTypeEntity.CONSTRUCTION
    WorkInterestTypeDomain.DRIVING -> WorkInterestTypeEntity.DRIVING
    WorkInterestTypeDomain.BEAUTY -> WorkInterestTypeEntity.BEAUTY
    WorkInterestTypeDomain.HOSPITALITY -> WorkInterestTypeEntity.HOSPITALITY
    WorkInterestTypeDomain.TECHNICAL -> WorkInterestTypeEntity.TECHNICAL
    WorkInterestTypeDomain.MANUFACTURING -> WorkInterestTypeEntity.MANUFACTURING
    WorkInterestTypeDomain.OFFICE -> WorkInterestTypeEntity.OFFICE
    WorkInterestTypeDomain.RETAIL -> WorkInterestTypeEntity.RETAIL
    WorkInterestTypeDomain.SPORTS -> WorkInterestTypeEntity.SPORTS
    WorkInterestTypeDomain.WAREHOUSING -> WorkInterestTypeEntity.WAREHOUSING
    WorkInterestTypeDomain.WASTE_MANAGEMENT -> WorkInterestTypeEntity.WASTE_MANAGEMENT
    WorkInterestTypeDomain.EDUCATION_TRAINING -> WorkInterestTypeEntity.EDUCATION_TRAINING
    WorkInterestTypeDomain.CLEANING_AND_MAINTENANCE -> WorkInterestTypeEntity.CLEANING_AND_MAINTENANCE
    WorkInterestTypeDomain.OTHER -> WorkInterestTypeEntity.OTHER
  }
}
